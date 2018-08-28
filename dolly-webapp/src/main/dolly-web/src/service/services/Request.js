import axios from 'axios'
import config from '~/config'

const instance = axios.create({ withCredentials: true })

const debugLoading = resolvedValue => {
	const DELAY_TIME = 300

	return new Promise(resolve => {
		if (config.debug) {
			setTimeout(() => resolve(resolvedValue), DELAY_TIME)
		} else {
			resolve(resolvedValue)
		}
	})
}

export default class Request {
	static get(url) {
		return instance.get(url).then(debugLoading)
	}

	static post(url, data) {
		return instance.post(url, data).then(debugLoading)
	}

	static put(url, data) {
		return instance.put(url, data).then(debugLoading)
	}

	static delete(url) {
		return instance.delete(url).then(debugLoading)
	}
}
