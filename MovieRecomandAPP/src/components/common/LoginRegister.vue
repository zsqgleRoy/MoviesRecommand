<template>
    <div class="login-register-container">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="登录" name="login"></el-tab-pane>
            <el-tab-pane label="注册" name="register"></el-tab-pane>
        </el-tabs>
        <el-form v-if="activeTab === 'login'" :model="loginForm" @submit.prevent="handleLogin">
            <el-form-item label="用户名" prop="username">
                <el-input v-model="loginForm.username" placeholder="用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input type="password" v-model="loginForm.password" placeholder="密码"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" native-type="submit">登录</el-button>
            </el-form-item>
            <el-alert v-if="loginError" title="错误提示" type="error" :closable="false">
                {{ loginError }}
            </el-alert>
        </el-form>
        <el-form v-else :model="registerForm" @submit.prevent="handleRegister">
            <el-form-item label="用户名" prop="username">
                <el-input v-model="registerForm.username" placeholder="用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="密码"></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" native-type="submit">注册</el-button>
            </el-form-item>
            <el-alert v-if="registerError" title="错误提示" type="error" :closable="false">
                {{ registerError }}
            </el-alert>
        </el-form>
    </div>
</template>

<script lang="ts" setup>
import { ElMessage } from 'element-plus';
import { ref } from 'vue';
import { useUserStore, type UserInfo } from '@/stores/user';
import { login, register } from '@/api';
import { useRouter } from 'vue-router';
const router = useRouter();

// 定义登录和注册表单的数据
const activeTab = ref('login');
const loginForm = ref({
    username: '',
    password: ''
});
const registerForm = ref({
    username: '',
    password: '',
    confirmPassword: ''
});
// 定义错误信息
const loginError = ref('');
const registerError = ref('');

// 获取用户状态管理 store
const userStore = useUserStore();

// 处理标签切换
const handleTabClick = (tab: { name: string; }) => {
    if (tab.name === 'login') {
        loginError.value = '';
    } else {
        registerError.value = '';
    }
};

// 处理登录逻辑
const handleLogin = async () => {
    const { username, password } = loginForm.value;
    
    try {
        const response = await login(loginForm.value.username, loginForm.value.password);
        if (response.code === 200) {
            const userInfoData: UserInfo = {
                userId: password,
                username: username
            };

            
            // 保存用户信息到 store
            // console.log(response.message)
            userStore.login(response.message, userInfoData); 
            // console.log(userInfoData);
            // userStore.logout(); 
            // console.log(userStore.userInfo);
            loginError.value = '';
            ElMessage.success('登录成功');
            router.push('/');
        } else {
            ElMessage.error("登录失败");
            loginError.value = response.message;
        }
    } catch (error) {
        ElMessage.error('登录请求出错，请稍后重试');
        loginError.value = '登录请求出错，请稍后重试';
    }
};

// 处理注册逻辑
const handleRegister = async () => {
    const { username, password, confirmPassword } = registerForm.value;
    if (username === '' || password === '') {
        ElMessage.error('用户名或密码为 NULL');
        registerError.value = '用户名或密码为 NULL';
        return;
    }
    if (password !== confirmPassword) {
        ElMessage.error('两次输入的密码不一致');
        registerError.value = '两次输入的密码不一致';
        return;
    }
    try {
        const response = await register(username, password);
        if (response.code === 200) {
            ElMessage.success('注册成功');
            registerError.value = '';
            activeTab.value = 'login';
        } else {
            ElMessage.error(response.message);
            registerError.value = response.message;
        }
    } catch (error) {
        ElMessage.error('注册请求出错，请稍后重试');
        registerError.value = '注册请求出错，请稍后重试';
    }
};
</script>
  
<style scoped>
  .login-register-container {
    width: 300px;
    margin: 50px auto;
  }
</style>
    