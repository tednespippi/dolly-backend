import Formatters from '~/utils/DataFormatter'
import _get from 'lodash/get'
import _isNil from 'lodash/isNil'

export const getAaregSuccessEnv = aaregStatusArray => {
	let envs = []
	if (!aaregStatusArray) return envs

	aaregStatusArray.length > 0 &&
		aaregStatusArray.forEach(status => {
			if (status.statusMelding === 'OK') {
				envs.push(Object.keys(status.environmentIdentsForhold))
			}
		})

	return envs
}

export const sokSelector = (items, searchStr) => {
	if (!items) return null
	const mappedItems = mapItems(items)

	if (!searchStr) return mappedItems

	const query = searchStr.toLowerCase()
	return mappedItems.filter(item => {
		const searchValues = [
			_get(item, 'id'),
			_get(item, 'antallIdenter'),
			_get(item, 'sistOppdatert'),
			_get(item, 'environments'),
			_get(item, 'ferdig')
		]
			.filter(v => !_isNil(v))
			.map(v => v.toString().toLowerCase())

		return searchValues.some(v => v.includes(query))
	})
}

const mapItems = items => {
	if (!items) return null
	return items.map(item => {
		return {
			...item,
			id: item.id.toString(),
			antallIdenter: item.antallIdenter.toString(),
			sistOppdatert: Formatters.formatDate(item.sistOppdatert),
			ferdig: item.stoppet
				? 'Stoppet'
				: bestillingIkkeFerdig(item)
					? 'Pågår'
					: harIkkeIdenter(item)
						? 'Feilet'
						: avvikStatus(item)
							? 'Avvik'
							: 'Ferdig'
		}
	})
}
const bestillingIkkeFerdig = item => !item.ferdig

const avvikStatus = item => {
	let avvik = false
	item.tpsfStatus &&
		item.tpsfStatus.map(status => {
			status.statusMelding !== 'OK' && (avvik = true)
		})
	item.aaregStatus &&
		item.aaregStatus.map(status => {
			status.statusMelding !== 'OK' && (avvik = true)
		})
	item.krrStubStatus &&
		item.krrStubStatus.map(status => {
			status.statusMelding !== 'OK' && (avvik = true)
		})
	item.sigrunStubStatus &&
		item.sigrunStubStatus.map(status => {
			status.statusMelding !== 'OK' && (avvik = true)
		})
	item.feil && (avvik = true)
	return avvik
}

const harIkkeIdenter = item => {
	let feilet = true
	item.tpsfStatus && (feilet = false)
	return feilet
}
