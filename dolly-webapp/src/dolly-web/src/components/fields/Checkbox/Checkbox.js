import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'
import cn from 'classnames'
import { Checkbox } from 'nav-frontend-skjema'

import './Checkbox.less'

export default class DollyCheckbox extends PureComponent {
	static propTypes = {
		id: PropTypes.string.isRequired,
		label: PropTypes.string
	}

	static defaultProps = {
		label: ''
	}

	render() {
		const { id, label, className, ...restProps } = this.props

		const cssClass = cn('dolly-checkbox', className, {
			nonlabel: label === ''
		})

		return <Checkbox id={id} className={cssClass} label={label} {...restProps} />
	}
}
