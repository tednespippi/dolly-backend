import React, { Fragment } from 'react'
import cn from 'classnames'
import KodeverkValueConnector from '~/components/fields/KodeverkValue/KodeverkValueConnector'
import StaticValue from '~/components/fields/StaticValue/StaticValue'
import { mapBestillingData } from './BestillingKriterieMapper'

const _renderBestillingsDetaljer = data => {
	return data.map((kategori, j) => {
		const bottomBorder = j != data.length - 1
		const cssClass = cn('flexbox--align-center info-text', {
			'bottom-border': bottomBorder
		})
		console.log('kategori :', kategori)
		if (kategori.header) {
			return (
				<Fragment key={j}>
					<h4>{kategori.header} </h4>
					{kategori.items && (
						<div className={cssClass}>
							{kategori.items.map((attributt, i) => {
								if (attributt.value) {
									return _renderStaticValue(attributt, i)
								} else if (attributt.header) {
									//F.eks. identhistorikk under partner
									return <div>{_renderBestillingsDetaljer([attributt])}</div>
								}
							})}
						</div>
					)}
					{kategori.itemRows && (
						<div className={cn('info-text', { 'bottom-border': bottomBorder })}>
							{kategori.itemRows.map((row, i) => {
								console.log('row :', row)
								return (
									<div className={'flexbox--align-start flexbox--wrap'} key={i}>
										{row.map((attributt, j) => {
											if (attributt.value) {
												return _renderStaticValue(attributt, j)
											}
										})}
									</div>
								)
							})}
						</div>
					)}
				</Fragment>
			)
		}
	})
}

const _renderStaticValue = (attributt, key) => {
	if (attributt.apiKodeverkId) {
		return (
			<KodeverkValueConnector
				apiKodeverkId={attributt.apiKodeverkId}
				showValue={attributt.showKodeverkValue}
				header={attributt.label}
				size={attributt.width ? attributt.width : 'small'}
				value={attributt.value}
				key={key}
			/>
		)
	}
	return (
		<StaticValue
			header={attributt.label}
			size={attributt.width ? attributt.width : 'small'}
			value={attributt.value}
			key={key}
		/>
	)
}

export default function Bestillingskriterier({ bestilling }) {
	const data = mapBestillingData(bestilling)

	if (!data) return <p>Kunne ikke hente bestillingsdata</p>

	return (
		<div>
			<h3>Bestillingskriterier</h3>
			{_renderBestillingsDetaljer(data)}
		</div>
	)
}
