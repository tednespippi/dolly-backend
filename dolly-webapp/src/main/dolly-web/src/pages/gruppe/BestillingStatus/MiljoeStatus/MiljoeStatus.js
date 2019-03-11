
import React, { PureComponent, Fragment } from 'react'
import './MiljoeStatus.less'
import '~/styles/utils.less'
import cn from 'classnames'
import Icon from '~/components/icon/Icon'
import Button from '~/components/button/Button'
import DollyModal from '~/components/modal/DollyModal'
import BestillingDetaljerModal from '~/components/bestillingDetaljerModal/BestillingDetaljerModal'


export default class MiljoeStatus extends PureComponent {
	constructor(props) {
		super(props)
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
		const { id, successEnvs, failedEnvs, bestilling, finnesFeilmelding, antallIdenterOpprettet } = this.props.miljoeStatusObj
		const failed = true && successEnvs.length == 0 && !finnesFeilmelding
		const { modalOpen } = this.state

		return (
			<div className="miljoe-status">
				<div className="status-header">
					<p>Bestilling #{id}</p>
					<h3>Bestillingsstatus</h3>
					<div className="remove-button-container">
						<Button kind="remove-circle" onClick={this.props.onCloseButton} />
					</div>
				</div>
				<hr />
				<div className={'miljoe-container miljoe-container-kolonne'}>
					{failed
						? this._renderFailureMessage(bestilling, antallIdenterOpprettet)
						: this._renderStatus(bestilling, successEnvs, failedEnvs, antallIdenterOpprettet)}
				</div>
				<div className="flexbox--all-center">
					<Button onClick={this.openModal} className="flexbox--align-center" kind="details">
						BESTILLINGSDETALJER
					</Button>
					<DollyModal
						isOpen={modalOpen}
						onRequestClose={this.closeModal}
						closeModal={this.closeModal}
						content={<BestillingDetaljerModal bestilling = {bestilling}/>}
						width={'60%'}
					/>
				</div>
			</div>
		)
	}

	_renderStatus = (bestilling, successEnvs, failedEnvs, antallIdenterOpprettet) => {
		return (
			<Fragment>
				{antallIdenterOpprettet < bestilling.antallIdenter && (
					<span className = 'miljoe-status error-text'>{antallIdenterOpprettet} av {bestilling.antallIdenter} bestilte identer ble opprettet i TPS.</span>
				)}
				<span className = 'miljoe-container miljoe-container-rad'>{this._renderMiljoeStatus(successEnvs, failedEnvs)}</span>
			</Fragment>
			)
	}

	_renderMiljoeStatus = (successEnvs, failedEnvs) => (
		<Fragment>
			{successEnvs.map((env, i) => {
				return this._renderMiljoe(env, i, 'success')
			})}

			{failedEnvs.map((env, i) => {
				return this._renderMiljoe(env, i, 'failed')
			})}
		</Fragment>
	)

	_renderFailureMessage = () => (
		<Fragment>
			<Icon kind={'report-problem-circle'} />
			<p>Bestillingen din ble ikke utført.</p>
		</Fragment>
	)

	_renderMiljoe = (env, key, status) => {
		const iconKind = status == 'success' ? 'feedback-check-circle' : 'report-problem-triangle'
		return (
			<div className={'miljoe'} key={key}>
				<Icon size={'24px'} kind={iconKind} />
				<p>{env}</p>
			</div>
		)
	}
}