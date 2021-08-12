import axios from 'axios'
// 执行 cnpm run build 的时候打开
// const baseURL = location.origin;
// 本地開發的時候用這個
const baseURL = "http://127.0.0.1:8115";
let http = axios.create({
    // `baseURL` 将自动加在 `url` 前面，除非 `url` 是一个绝对 URL。
    // 它可以通过设置一个 `baseURL` 便于为 axios 实例的方法传递相对 URL
    baseURL: baseURL,
    // `withCredentials` 表示跨域请求时是否需要使用凭证
    withCredentials: true,
    // `headers` 是即将被发送的自定义请求头
    headers: {
        /*'X-Requested-With': 'XMLHttpRequest',*/
        /*'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',*/
        'Content-Type': 'application/json;charset-UTF-8',
        /*"Access-Control-Allow-Credentials": true,*/
        /*'Access-Control-Allow-Origin': '*',*/
        /*'Access-Control-Allow-Methods': 'DELETE,PUT,POST,GET,OPTIONS',*/
    },
    // `transformRequest` 允许在向服务器发送前，修改请求数据
    // 只能用在 "PUT", "POST" 和 "PATCH" 这几个请求方法
    // 后面数组中的函数必须返回一个字符串，或 ArrayBuffer，或 Stream
    /*transformRequest: [function (data) {
        let newData = '';
        for (let k in data) {
            // eslint-disable-next-line no-prototype-builtins
            if (data.hasOwnProperty(k) === true) {
                newData += encodeURIComponent(k) + '=' + encodeURIComponent(data[k]) + '&';
            }
        }
        return newData;
    }],*/
    /*proxy: {
        host: "127.0.0.1",
        port: 8082,
    }*/
});

// 添加请求拦截器
axios.interceptors.request.use(config => {
    // config 请求配置
    // console.log("请求拦截器");
    return config
}, err => {
    return Promise.reject(err)
});

// 添加响应拦截器
axios.interceptors.response.use(res => {
    // res 响应结果
    // console.log("响应拦截器");
    return res
}, err => {
    return Promise.reject(err)
});

function apiAxios(method, url, params, response) {
    http({
        method: method,
        url: url,
        data: method === 'POST' || method === 'PUT' ? params : null,
        params: method === 'GET' || method === 'DELETE' ? params : null,
    }).then(function (res) {
        response(res);
    }).catch(function (err) {
        response(err);
    })
}

const api = {
    get: function (url, params, response) {
        return apiAxios('GET', url, params, response)
    },
    post: function (url, params, response) {
        return apiAxios('POST', url, params, response)
    },
    put: function (url, params, response) {
        return apiAxios('PUT', url, params, response)
    },
    delete: function (url, params, response) {
        return apiAxios('DELETE', url, params, response)
    },
    syncGet: async function(url, params) {
        let newData = '';
        for (let k in params) {
            // eslint-disable-next-line no-prototype-builtins
            if (params.hasOwnProperty(k) === true) {
                newData += encodeURIComponent(k) + '=' + encodeURIComponent(params[k]) + '&';
            }
        }
        newData = newData.substring(0, newData.lastIndexOf("&"));
        return await axios.get(baseURL + url + "?" + newData);
    }
};

export default api;