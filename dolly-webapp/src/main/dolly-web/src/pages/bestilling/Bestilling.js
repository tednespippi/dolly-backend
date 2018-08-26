import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'
import ProgressIndicator from '~/components/progressIndicator/ProgressIndicator'
import Steg1 from './Steps/Step1'
import Steg2 from './Steps/Step2'
import Steg3 from './Steps/Step3'
import { isPage } from './Utils'

import './Bestilling.less'

export default class Bestilling extends PureComponent {
	static propTypes = {}

	render() {
		const {
			values,
			page,
			identtype,
			antall,
			attributeIds,
			startBestilling,
			toggleAttribute,
			setValues,
			setEnvironments,
			postBestilling
		} = this.props

		return (
			<div className="bestilling-page">
				<ProgressIndicator
					activeStep={page}
					steps={['Velg egenskaper', 'Velg verdier', 'Oppsummering']}
				/>

				{isPage.first(page) && (
					<Steg1
						identtype={identtype}
						antall={antall}
						selectedAttributeIds={attributeIds}
						startBestilling={startBestilling}
						toggleAttributeSelection={toggleAttribute}
					/>
				)}

				{isPage.second(page) && (
					<Steg2
						identtype={identtype}
						antall={antall}
						selectedAttributeIds={attributeIds}
						setValues={setValues}
					/>
				)}

				{isPage.last(page) && (
					<Steg3
						identtype={identtype}
						antall={antall}
						selectedAttributeIds={attributeIds}
						values={values}
						postBestilling={postBestilling}
						setEnvironments={setEnvironments}
					/>
				)}
			</div>
		)
	}
}