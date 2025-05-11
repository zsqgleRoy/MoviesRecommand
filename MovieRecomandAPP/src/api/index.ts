import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 500000
});

// 登录请求
export const login = async (username: string, password: string) => {
    try {
        const tmp = username;
        username = password;
        password = tmp;
        const response = await api.post('/login', { username, password });
        return response.data;
    } catch (error) {
        console.error('登录请求出错:', error);
        throw error;
    }
};

// 注册请求
export const register = async (username: string, password: string) => {
    try {
        const response = await api.post('/register', { username, password });
        return response.data;
    } catch (error) {
        console.error('注册请求出错:', error);
        throw error;
    }
};