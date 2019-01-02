import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'
import cn from 'classnames'
import Icon from '~/components/icon/Icon'
import Knapp from 'nav-frontend-knapper'

import './Button.less'

export default class Button extends PureComponent {
	static propTypes = {
		kind: PropTypes.string,
		onClick: PropTypes.func
	}

	static defaultProps = {
		kind: null,
		onClick: () => {} // Default noop func
	}

	onClickHandler = event => {
		event.stopPropagation()
		return this.props.onClick()
	}

	render() {
		const { kind, children, className, type = 'button', title, onMouseEnter } = this.props

		const cssClass = cn('dolly-button', className)

		return (
			<button
				onMouseEnter={onMouseEnter}
				onMouseLeave={onMouseEnter}
				type={type}
				className={cssClass}
				onClick={this.onClickHandler}
				title={title}
			>
				{kind && <Icon kind={kind} />}
				{children && children}
			</button>
		)
	}
}
