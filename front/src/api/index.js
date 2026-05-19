import axios from 'axios';

// 创建axios实例

const config = {
    baseURL: 'http://localhost:9090/', // api的base_url
    timeout: 5000 // 请求超时时间
}


class ReuqestHttp {
    service = null;
    constructor(config) {
        this.service = axios.create(config);

        // 请求拦截器
        this.service.interceptors.request.use(
            config => {
                // 自动附带 Authorization header
                const token = localStorage.getItem('token')
                if (token) {
                    config.headers['Authorization'] = 'Bearer ' + token
                }
                return config;
            },
            error => {
                // 请求错误处理
                console.log('what ???')
                console.log(error); // for debug
                Promise.reject(error);
            }
        );

        // 响应拦截器
        this.service.interceptors.response.use(
            response => {
                // 对响应数据做处理，例如只返回data部分
                const res = response
                // 如果返回的HTTP状态码为200，说明网络请求成功，返回后端body
                if (res.status === 200) {
                    return res.data
                } else {
                    // 其他状态码都当作错误处理
                    return Promise.reject(res.data || { status: res.status, msg: 'Error' })
                }
            },
            error => {
                // 对响应错误做处理
                console.log('what ???')
                console.log('err' + error); // for debug
                return Promise.reject(error);
            }
        );
    }
    get(url, params, headers) {
        return this.service.get(url, {
            params,
            headers
        })
    }
    post(url, data, headers) {
        return this.service.post(url, data, headers)
    }
    put(url, data, headers) {
        return this.service.put(url, data, headers)
    }
    delete(url, headers) {
        return this.service.delete(url, headers)
    }
    // download .etc you can add more methods here
}
export default new ReuqestHttp(config);
