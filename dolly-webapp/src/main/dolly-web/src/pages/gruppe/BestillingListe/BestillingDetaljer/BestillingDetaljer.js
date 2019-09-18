import React, { PureComponent, Fragment } from 'react'
import Knapp from 'nav-frontend-knapper'
import { Formik, FieldArray } from 'formik'
import * as yup from 'yup'
import Button from '~/components/ui/button/Button'
import './BestillingDetaljer.less'
import Formatters from '~/utils/DataFormatter'
import StaticValue from '~/components/fields/StaticValue/StaticValue'
import MiljoVelgerConnector from '~/components/miljoVelger/MiljoVelgerConnector'
import SendOpenAmConnector from '~/pages/gruppe/SendOpenAm/SendOpenAmConnector'
import OpenAmStatusConnector from '~/pages/gruppe/OpenAmStatus/OpenAmStatusConnector'
import DollyModal from '~/components/ui/modal/DollyModal'
import BestillingSammendrag from '~/components/bestilling/sammendrag/Sammendrag'

export default class BestillingDetaljer extends PureComponent {
	constructor(props) {
		super(props)

		this.EnvValidation = yup.object().shape({
			environments: yup.array().required('Velg minst ett miljø')
		})
		this.state = {
			modalOpen: false
		}
	}

	openModal = () => {
		this.setState({ modalOpen: true })
	}
	closeModal = () => {
		this.setState({ modalOpen: false })
	}

	render() {
		const { bestilling, openAm, openAmState } = this.props
		const { modalOpen } = this.state
		const bestillingId = bestilling.id

		let openAmRes
		if (openAmState.responses.length > 0) {
			openAmRes = openAmState.responses.find(response => response.id == bestillingId)
		}

		return (
			<div className="bestilling-detaljer">
				<BestillingSammendrag bestilling={bestilling} />
				{openAm ? (
					<div className="bestilling-detaljer">
						<h3>Jira-lenker</h3>
						<div className={'jira-link'}>{this._renderJiraLinks(openAm)}</div>
					</div>
				) : (
					openAmRes && (
						<OpenAmStatusConnector
							id={bestillingId}
							lukket={openAmRes.lukket}
							responses={this._renderOpenAmStateResponses(openAmRes)}
							className="open-am-status"
						/>
					)
				)}
				<div className="flexbox--align-center--justify-end info-block">
					<div className="flexbox--align-center--justify-end">
						{this._erIdentOpprettet() &&
							!openAm &&
							(!openAmRes && (
								<SendOpenAmConnector
									bestillingId={bestillingId}
									className="flexbox--align-center"
								/>
							))}
					</div>
					<div className="flexbox--align-center--justify-end">
						{this._erIdentOpprettet() && (
							<Button onClick={this.openModal} className="flexbox--align-center" kind="synchronize">
								GJENOPPRETT
							</Button>
						)}
						<DollyModal isOpen={modalOpen} closeModal={this.closeModal}>
							{this._renderGjenopprettModal()}
						</DollyModal>
					</div>
				</div>
			</div>
		)
	}

	_renderOpenAmStateResponses = openAmState => {
		return openAmState.data.map(response => response.message)
	}

	_erIdentOpprettet = () => {
		const { bestilling } = this.props
		let temp = false

		if (bestilling.tpsfStatus) {
			bestilling.tpsfStatus.forEach(status => {
				if (status.environmentIdents) temp = true
			})
		}
		return temp
	}

	_renderJiraLinks = openAm => {
		const data = openAm.split(',')
		const linkArray = data.map((link, i) => {
			return (
				<Fragment key={i}>
					<a href={link} target="_blank">
						{link.substring(28, link.length)}
					</a>
					{i !== data.length - 1 && <p>, </p>}
				</Fragment>
			)
		})
		return linkArray
	}

	_renderGjenopprettModal = () => {
		const { environments, id } = this.props.bestilling // miljø som ble bestilt i en bestilling

		return (
			<Fragment>
				<div className="dollymodal">
					<div style={{ paddingLeft: 20, paddingRight: 20 }}>
						<h1>Bestilling #{id}</h1>
						<StaticValue header="Bestilt miljø" value={Formatters.arrayToString(environments)} />
						<br />
						<hr />
					</div>
					<Formik
						initialValues={{
							environments
						}}
						onSubmit={this._submitFormik}
						validationSchema={this.EnvValidation}
						render={formikProps => {
							return (
								<Fragment>
									<FieldArray
										name="environments"
										render={arrayHelpers => (
											<MiljoVelgerConnector
												heading={'Velg miljø å gjenopprette i'}
												arrayHelpers={arrayHelpers}
												arrayValues={formikProps.values.environments}
											/>
										)}
									/>
									<div className="dollymodal_buttons">
										<Knapp autoFocus type="standard" onClick={this.closeModal}>
											Avbryt
										</Knapp>
										<Knapp type="hoved" onClick={formikProps.submitForm}>
											Utfør
										</Knapp>
									</div>
								</Fragment>
							)
						}}
					/>
				</div>
			</Fragment>
		)
	}

	_submitFormik = async values => {
		const envsQuery = Formatters.arrayToString(values.environments)
			.replace(/ /g, '')
			.toLowerCase()
		await this.props.gjenopprettBestilling(envsQuery)
		await this.props.getBestillinger()
	}

	_onToggleModal = () => {
		this.setState({ modalOpen: !this.state.modalOpen })
	}
}
