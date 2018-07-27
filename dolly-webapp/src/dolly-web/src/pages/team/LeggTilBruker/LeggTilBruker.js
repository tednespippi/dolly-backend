import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'
import { FormikDollySelect } from '~/components/fields/Select/Select'
import { FormikInput } from '~/components/fields/Input/Input'
import { Formik, Form, Field, getIn } from 'formik'
import { DollyApi } from '~/service/Api'
import DisplayFormikState from '~/utils/DisplayFormikState'
import Knapp from 'nav-frontend-knapper'
import FormErrors from '~/components/formErrors/FormErrors'
import * as yup from 'yup'
import Table from '~/components/table/Table'
import Loading from '~/components/loading/Loading'

export default class RedigerTeam extends PureComponent {
	static propTypes = {}

	erRedigering = Boolean(getIn(this.props.team, 'id', false))

	onHandleSubmit = async (values, actions) => {
		const { createTeam, updateTeam, team } = this.props

		values.navIdent = values.navIdent.map(user => user.value)
		console.log(values)
		// const res = this.erRedigering ? await updateTeam(team.id, values) : await createTeam(values)
	}

	validation = () =>
		yup.object().shape({
			navIdent: yup.string().required('Bruker er et påkrevd felt')
		})

	render() {
		const { closeLeggTilBruker, team, createOrUpdateFetching } = this.props
		// if (createOrUpdateFetching) {
		// 	return (
		// 		<Table.Row>
		// 			<Loading label="oppdaterer gruppe" />
		// 		</Table.Row>
		// 	)
		// }

		let initialValues = {
			navIdent: ''
		}

		return (
			<Formik
				initialValues={initialValues}
				validationSchema={this.validation}
				onSubmit={this.onHandleSubmit}
				render={props => {
					const { values, touched, errors, dirty, isSubmitting } = props
					return (
						<Form className="opprett-tabellrad" autoComplete="off">
							<h2>Legg til bruker</h2>
							<div className="fields">
								<Field
									name="navIdent"
									label="Velg en bruker"
									component={FormikDollySelect}
									multi={true}
									loadOptions={() =>
										DollyApi.getBrukere().then(DollyApi.Utils.NormalizeBrukerListForDropdown)
									}
								/>

								<Knapp type="hoved" htmlType="submit">
									Legg til
								</Knapp>
								<Knapp type="standard" htmlType="button" onClick={closeLeggTilBruker}>
									Avbryt
								</Knapp>
							</div>

							{/* <FormErrors errors={errors} touched={touched} /> */}
							{/* <DisplayFormikState {...props} /> */}
						</Form>
					)
				}}
			/>
		)
	}
}
