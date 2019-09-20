import React, { useState } from 'react'

export default function UseBoolean(initial = false) {
	const [isOn, setIsOn] = useState(initial)
	const turnOn = () => setIsOn(true)
	const turnOff = () => setIsOn(false)
	return [isOn, turnOn, turnOff]
}
