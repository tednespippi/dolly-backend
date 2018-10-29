import { Kategorier, SubKategorier } from '../Categories'
import { Attributt, InputType, DataSource } from '../Types'
import Formatters from '~/utils/DataFormatter'
import SelectOptionsManager from '~/service/kodeverk/SelectOptionsManager/SelectOptionsManager'
import DateValidation from '~/components/fields/Datepicker/DateValidation'

import * as yup from 'yup'

const AttributtListe: Attributt[] = [
	// {
	// 	hovedKategori: Kategorier.Inntekter,
	// 	subKategori: SubKategorier.Inntekt,
	// 	id: 'inntekter',
	// 	path: 'inntekter.inntekt',
	// 	label: 'Kun inntektsbeløp',
	// 	dataSource: DataSource.SIGRUN,
	// 	validation: yup.object(),
	// 	items: [
	// 		{
	// 			hovedKategori: Kategorier.Inntekt,
	// 			subKategori: SubKategorier.Inntekt,
	// 			id: 'beløp',
	// 			label: 'Beløp',
	// 			path: 'inntekt.belop',

	// 			dataSource: DataSource.SIGRUN,
	// 			inputType: InputType.Number,
	// 			validation: yup.number().required('Oppgi en sum.')
	// 		},
	// 		{
	// 			hovedKategori: Kategorier.Inntekt,
	// 			subKategori: SubKategorier.Inntekt,
	// 			path: 'inntekt.aar',
	// 			id: 'År',
	// 			label: 'År',
	// 			dataSource: DataSource.SIGRUN,
	// 			inputType: InputType.Date,
	// 			validation: DateValidation
	// 		}
	// 	]
	// },
	{
		hovedKategori: Kategorier.Inntekter,
		subKategori: SubKategorier.Inntekt,
		id: 'Beregnet skatt',
		label: 'Beregnet skatt',
		dataSource: DataSource.SIGRUN,
		validation: yup.object(),
		items: [
			{
				hovedKategori: Kategorier.Inntekt,
				subKategori: SubKategorier.Inntekt,
				id: 'typeinntekt',
				label: 'Type inntekt',
				path: 'tekniskNavn',
				dataSource: DataSource.SIGRUN,
				inputType: InputType.Select,
				apiKodeverkId: 'Beregnet skatt',
				// options: [{ label: 'test', value: 'test' }],
				validation: yup.string().required('Velg en type inntekt.')
			},
			{
				hovedKategori: Kategorier.Inntekt,
				subKategori: SubKategorier.Inntekt,
				path: 'verdi',
				id: 'beloep',
				label: 'Beløp',
				dataSource: DataSource.SIGRUN,
				inputType: InputType.Number,
				validation: yup
					.number()
					.min(1, 'Tast inn et gyldig beløp')
					.required('Oppgi beløpet')
			},
			{
				hovedKategori: Kategorier.Inntekt,
				subKategori: SubKategorier.Inntekt,
				id: 'inntektsaar',
				label: 'År',
				path: 'inntektsaar',
				dataSource: DataSource.SIGRUN,
				inputType: InputType.Number,
				validation: yup.number().required('Tast inn et gyldig år')
			}
		]
	},
	{
		hovedKategori: Kategorier.Inntekter,
		subKategori: SubKategorier.Inntekt,
		id: 'Summert skattegrunnlag',
		path: 'inntekter.inntekt',
		label: 'Summert skattegrunnlag',
		dataSource: DataSource.SIGRUN,
		validation: yup.object(),
		items: [
			{
				hovedKategori: Kategorier.Inntekt,
				subKategori: SubKategorier.Inntekt,
				id: 'typeinntekt',
				label: 'Type inntekt',
				dataSource: DataSource.SIGRUN,
				inputType: InputType.Select,
				apiKodeverkId: 'Summert skattegrunnlag',
				// options: [{ label: 'test', value: 'test' }],
				validation: yup.string().required('Velg en type inntekt.')
			},
			{
				hovedKategori: Kategorier.Inntekt,
				subKategori: SubKategorier.Inntekt,
				id: 'beloep',
				label: 'Beløp',
				dataSource: DataSource.SIGRUN,
				inputType: InputType.Number,
				validation: yup
					.number()
					.min(1, 'Tast inn et gyldig beløp')
					.required('Oppgi beløpet')
			},
			{
				hovedKategori: Kategorier.Inntekt,
				subKategori: SubKategorier.Inntekt,
				id: 'inntektsaar',
				label: 'År',
				dataSource: DataSource.SIGRUN,
				inputType: InputType.Number,
				validation: yup.number().required('Tast inn et gyldig år')
			}
		]
	}
]

export default AttributtListe
