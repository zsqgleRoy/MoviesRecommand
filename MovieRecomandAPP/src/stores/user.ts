import { defineStore } from 'pinia';

export interface UserInfo {
  userId: string; // 假设用户 ID
  username: string; // 用户名
  email?: string; // 可选字段
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    userInfo: null as UserInfo | null,
    userInfos: localStorage.getItem('userInfo') || null,
  }),
  actions: {
    // 登录时保存 token 和用户信息
    login(token: string, userInfo: UserInfo) {
      this.token = token;
      this.userInfo = userInfo;
      this.userInfos = localStorage.getItem('userInfo') || null;
      localStorage.setItem('token', token);
      localStorage.setItem('userInfo', JSON.stringify(userInfo)); // 持久化存储（可选）
      this.userInfos = localStorage.getItem('userInfo') || null;
    },
    // 登出时清空数据
    logout() {
      this.token = null;
      this.userInfo = null;
      this.userInfos = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
    },
  },
  getters: {
    isLoggedIn: (state) => !!state.token, // 是否登录
  },
});