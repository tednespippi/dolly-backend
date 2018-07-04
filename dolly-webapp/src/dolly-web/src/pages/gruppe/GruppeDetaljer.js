import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'
import IconButton from '~/components/fields/IconButton/IconButton'

export default class GruppeDetaljer extends PureComponent {
	static propTypes = {
		data: PropTypes.object
	}

	state = {
		expanded: false
	}

	toggleExpanded = () => this.setState({ expanded: !this.state.expanded })

	render() {
		const { data } = this.props

		const iconType = this.state.expanded ? 'chevron-up' : 'chevron-down'

		return (
			<div className="gruppe-detaljer">
				<div className="gd-blokker">
					<DetaljBlokk header="EIER" value={data.eier} />
					<DetaljBlokk header="TEAM" value={data.team} />
					<DetaljBlokk header="TESTMILJØ" value={data.env} />
					<DetaljBlokk header="ANTALL PERSONER" value={data.personer_num} />
					{this.state.expanded && [
						<DetaljBlokk header="MENN" value={data.menn_num} />,
						<DetaljBlokk header="KVINNER" value={data.kvinner_num} />,
						<DetaljBlokk header="OPPRETTET" value={data.opprettet} />,
						<DetaljBlokk header="SIST ENDRET" value={data.sistEndret} />,
						<DetaljBlokk header="HENSIKT" value={data.hensikt} />
					]}
				</div>
				<IconButton kind={iconType} onClick={this.toggleExpanded} />
			</div>
		)
	}
}

const DetaljBlokk = ({ header, value }) => (
	<div className="gd-blokk">
		<h5>{header}</h5>
		<span>{value}</span>
	</div>
)