import React, { PureComponent } from 'react'
import PropTypes from 'prop-types'
import Select, { Async } from 'react-select'
import { getIn } from 'formik'

import './Select.less'

export default class DollySelect extends PureComponent {
	static propTypes = {
		name: PropTypes.string.isRequired,
		label: PropTypes.string.isRequired,
		placeholder: PropTypes.string
	}

	static defaultProps = {
		placeholder: 'Velg..'
	}

	translations = () => ({
		clearValueText: 'Fjern verdi',
		clearAllText: 'Fjern alle',
		noResultsText: 'Listen er tom',
		searchPromptText: 'Skriv inn for å søke',
		loadingPlaceholder: 'Laster..'
	})

	render() {
		const { name, label, placeholder, loadOptions, ...restProps } = this.props

		return (
			<div className="skjemaelement dollyselect">
				<label className="skjemaelement__label">{label} </label>
				<div className="dollyselect-input">
					{loadOptions ? (
						<Async
							name={name}
							loadOptions={loadOptions}
							placeholder={placeholder}
							clearable
							openOnFocus
							{...this.translations}
							{...restProps}
						/>
					) : (
						<Select
							name={name}
							placeholder={placeholder}
							clearable
							openOnFocus
							{...this.translations}
							{...restProps}
						/>
					)}
				</div>
			</div>
		)
	}
}

export const FormikDollySelect = props => {
	const { field, form, ...restProps } = props

	return (
		<DollySelect
			name={field.name}
			value={field.value}
			onChange={selected => form.setFieldValue(field.name, getIn(selected, 'value', selected))}
			onBlur={e => form.setFieldTouched(field.name, true)}
			{...restProps}
		/>
	)
}