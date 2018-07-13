import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { ToggleGruppe, ToggleKnapp } from 'nav-frontend-skjema'
import Overskrift from '~/components/overskrift/Overskrift'
import Input from '~/components/fields/Input/Input'
import RedigerTeamConnector from './RedigerTeam/RedigerTeamConnector'
import TeamListe from './TeamListe'

export default class TeamOversikt extends Component {
	static propTypes = {
		teams: PropTypes.object
	}

	componentDidMount() {
		this.props.fetchTeams()
	}

	handleViewChange = e => {
		this.props.setTeamVisning(e.target.value)
		this.props.fetchTeams()
	}

	render() {
		const { teams, history, startOpprettTeam, startRedigerTeam } = this.props
		const { items, fetching, visning, visOpprettTeam, editTeamId } = teams

		return (
			<div className="oversikt-container">
				<div className="flexbox--space">
					<Overskrift label="Teams" actions={[{ icon: 'add-circle', onClick: startOpprettTeam }]} />
					<Input name="sokefelt" className="label-offscreen" label="" placeholder="Søk" />
				</div>

				<div className="flexbox--space">
					<ToggleGruppe onChange={this.handleViewChange} name="toggleGruppe">
						<ToggleKnapp value="mine" checked={visning === 'mine'} key="1">
							Mine
						</ToggleKnapp>
						<ToggleKnapp value="alle" checked={visning === 'alle'} key="2">
							Alle
						</ToggleKnapp>
					</ToggleGruppe>
				</div>
				{visOpprettTeam && <RedigerTeamConnector />}
				<TeamListe
					items={items}
					fetching={fetching}
					history={history}
					startRedigerTeam={startRedigerTeam}
					editTeamId={editTeamId}
				/>
			</div>
		)
	}
}