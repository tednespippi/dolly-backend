import {
	AttributtGruppe,
	AttributtGruppeHovedKategori,
	Attributt,
	Kategori,
	AttributtType
} from './Types'
import * as yup from 'yup'
import { FormikValues } from 'formik'
import AttributtListe from './Attributter'
import { groupList, groupListByHovedKategori } from './GroupList'
import DataFormatter from '~/utils/DataFormatter'
import DataSourceMapper from '~/utils/DataSourceMapper'
import _set from 'lodash/set'
import _get from 'lodash/get'
import _has from 'lodash/has'
import { isAttributtEditable } from './AttributtHelpers'

export default class AttributtManager {
	// BASE FUNCTIONS
	listAllSelected(selectedIds: string[]): Attributt[] {
		return AttributtListe.filter(f => selectedIds.includes(f.parent || f.id))
	}

	listAllExcludingChildren(): Attributt[] {
		return AttributtListe.filter(f => !f.parent)
	}

	listSelectedExcludingChildren(selectedIds: string[]): Attributt[] {
		return AttributtListe.filter(f => !f.parent && selectedIds.includes(f.id))
	}

	//STEP 1
	listSelectableAttributes(searchTerm: string): AttributtGruppe[] {
		const list = this.listAllExcludingChildren()
		return groupList(
			searchTerm ? list.filter(f => f.label.toLowerCase().includes(searchTerm.toLowerCase())) : list
		)
	}

	listUtvalg(selectedIds: string[]): AttributtGruppeHovedKategori[] {
		return groupListByHovedKategori(this.listSelectedExcludingChildren(selectedIds))
	}

	//STEP 2 + 3
	listSelectedAttributesForValueSelection(selectedIds: string[]): AttributtGruppe[] {
		return groupList(this.listAllSelected(selectedIds))
	}

	getValidations(selectedIds: string[]): yup.MixedSchema {
		// Get all selected attributes that has validations
		const list = this.listAllSelected(selectedIds).filter(s => s.validation)
		return this._createValidationObject(list)
	}

	getInitialValues(selectedIds: string[], values: object): FormikValues {
		return this._getListOfInitialValues(this.listAllSelected(selectedIds), values)
	}

	//Edit attributes
	listEditableFlat(values: object, ident: string, dataSources: string[]): Attributt[] {
		return AttributtListe.filter(attr => {
			// return early if attribute is not editable
			if (!isAttributtEditable(attr)) return false

			const { dataSource, path, id, editPath } = attr
			// check for datasource
			if (!dataSources.includes(dataSource)) return false

			const dataSourceValues =
				values[DataSourceMapper(dataSource)][0] || values[DataSourceMapper(dataSource)][ident]
			// check for values
			if (!dataSourceValues) return false

			const dataPath = editPath || path || id
			// check if value exists (not NULL)
			if(_get(dataSourceValues, dataPath)) {
				return _get(dataSourceValues, dataPath)
			}
		})
	}

	listEditable(values: object, ident: string, dataSources: string[]): AttributtGruppe[] {
		return groupList(this.listEditableFlat(values, ident, dataSources))
	}

	getValidationsForEdit(values: object, ident: string, dataSources: string[]): yup.MixedSchema {
		// editable attributter, som kun er read skal ikke ha validering.
		const list = this.listEditableFlat(values, ident, dataSources)
		return this._createValidationObject(list, true)
	}

	//TODO: Se om vi dette kan gjøres ryddigere, litt rotete pga tpsf er array mens andre registre er object
	getInitialValuesForEditableItems(
		values: object,
		ident: string,
		dataSources: string[]
	): FormikValues {
		const editableAttributes = this.listEditableFlat(values, ident, dataSources)
		return editableAttributes.reduce((prev, item) => {
			const dataSource = DataSourceMapper(item.dataSource)
			const sourceValues =
				dataSource === 'tpsf'
					? values[dataSource][0]
					: values[dataSource] && values[dataSource][ident]

			if (item.items) {
				return this._setInitialArrayValuesFromServer(prev, item, sourceValues)
			}

			return this._setInitialValueFromServer(prev, item, sourceValues)
		}, {})
	}

	getAttributtListByHovedkategori(hovedkategori: Kategori): string[] {
		return AttributtListe.filter(attr => attr.hovedKategori.id === hovedkategori.id).map(
			attr => attr.id
		)
	}

	getParentAttributtListByHovedkategori(hovedkategori: Kategori): string[] {
		return AttributtListe.filter(
			attr => attr.hovedKategori.id === hovedkategori.id && !attr.parent
		).map(attr => attr.id)
	}

	getAttributtById(attributtId: string): Attributt {
		return AttributtListe.find(attr => attr.id === attributtId)
	}

	_createValidationObject(list: Attributt[], editMode = false): yup.MixedSchema {
		// Reduce to item.id and validation to create a validation object
		const validationObject = list.reduce((accumulator, currentObject) => {
			if (currentObject.items) {
				let itemsToValidate = currentObject.items
				if (editMode)
					itemsToValidate = itemsToValidate.filter(
						attr => attr.attributtType !== AttributtType.SelectAndRead
					)
				const mapItemsToObject = this._mapArrayToObjectWithValidation(itemsToValidate)

				return {
					...accumulator,
					[currentObject.id]: yup.array().of(yup.object().shape(mapItemsToObject))
				}
			}
			return _set(accumulator, currentObject.id, currentObject.validation)
		}, {})
		return yup.object().shape(validationObject)
	}

	_getListOfInitialValues(list, values) {
		return list.reduce((prev, item) => {
			// Array
			if (item.items) {
				const mapItemsToObject = this._mapArrayToObjectWithEmptyValues(item.items)
				return this._setInitialArrayValue(prev, item.id, values, [mapItemsToObject])
			}
			// Flattened object -> Ignore parent that has no inputType
			if (!item.inputType) return prev

			// Initvalue based on key-value
			return this._setInitialValueFromState(prev, item, values)
		}, {})
	}
	_setInitialValueFromState(currentObject, item, stateValues) {
		let initialValue = this.initValueSelector(item)
		const fromState = _get(stateValues, item.id)
		if (fromState || fromState === false) initialValue = fromState

		return _set(currentObject, item.id, initialValue)
	}

	_setInitialValueFromServer(currentObject, item, serverValues) {
		let initialValue = this.initValueSelector(item)
		const fromState = _get(serverValues, item.path || item.id)
		if (fromState || fromState === false) initialValue = fromState

		if (item.inputType === 'date') initialValue = DataFormatter.formatDate(initialValue)

		return _set(currentObject, item.id, initialValue)
	}

	_setInitialArrayValuesFromServer(currentObject, item, serverValues) {
		// kanskje alle skal kunne redigeres
		const itemArray = item.items
		const editableAttributes = itemArray.filter(item => isAttributtEditable(item))
		const arrayValues = serverValues.map(valueObj => {
			return editableAttributes.reduce((prev, curr) => {
				const currentPath = curr.editPath || curr.path
				return _set(prev, curr.id, valueObj[currentPath])
			}, {})
		})
		return _set(currentObject, item.id, arrayValues)
	}

	_setInitialArrayValue(currentObject, itemId, stateValues, array) {
		let initialValue = array
		const fromState = _get(stateValues, itemId)
		if (fromState || fromState === false) initialValue = fromState

		return _set(currentObject, itemId, initialValue)
	}

	_mapArrayToObjectWithEmptyValues = list => {
		return list.reduce((accumulator, item) => {
			return _set(accumulator, item.id, this.initValueSelector(item))
		}, {})
	}

	_mapArrayToObjectWithValidation = list => {
		return list.reduce((accumulator, item) => {
			return _set(accumulator, item.id, item.validation)
		}, {})
	}

	initValueSelector = item => {
		// TODO: Åpne for defaultValue på Attributt?
		if (item.id.includes('identtype')) {
			return 'FNR'
		}
		// TODO: avklaring: skal alle datofelter settes automatisk til dagens dato?
		switch (item.inputType) {
			case 'date':
				return DataFormatter.formatDate(new Date())
			case 'number':
				return 0
			default:
				return ''
		}
	}
}
