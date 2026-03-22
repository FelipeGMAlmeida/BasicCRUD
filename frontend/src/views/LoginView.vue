<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const mode = ref('login') // 'login' | 'register'
const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    if (mode.value === 'login') {
      await auth.login(email.value, password.value)
    } else {
      await auth.register(name.value, email.value, password.value)
    }
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || 'Erro ao autenticar. Verifique suas credenciais.'
  } finally {
    loading.value = false
  }
}

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  error.value = ''
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>BasicCRUD</h1>
      <h2>{{ mode === 'login' ? 'Entrar' : 'Criar conta' }}</h2>

      <form @submit.prevent="submit">
        <div v-if="mode === 'register'" class="field">
          <label>Nome</label>
          <input v-model="name" type="text" placeholder="Seu nome" required />
        </div>

        <div class="field">
          <label>E-mail</label>
          <input v-model="email" type="email" placeholder="seu@email.com" required />
        </div>

        <div class="field">
          <label>Senha</label>
          <input v-model="password" type="password" placeholder="••••••" required minlength="6" />
        </div>

        <p v-if="error" class="error">{{ error }}</p>

        <button type="submit" :disabled="loading">
          {{ loading ? 'Aguarde...' : mode === 'login' ? 'Entrar' : 'Cadastrar' }}
        </button>
      </form>

      <p class="toggle">
        {{ mode === 'login' ? 'Não tem conta?' : 'Já tem conta?' }}
        <a href="#" @click.prevent="toggleMode">
          {{ mode === 'login' ? 'Cadastre-se' : 'Fazer login' }}
        </a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.login-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.12);
  padding: 2.5rem 2rem;
  width: 100%;
  max-width: 380px;
}
h1 { margin: 0 0 0.25rem; font-size: 1.6rem; color: #42b883; }
h2 { margin: 0 0 1.5rem; font-size: 1.1rem; color: #666; font-weight: 400; }
.field { display: flex; flex-direction: column; margin-bottom: 1rem; }
.field label { font-size: 0.85rem; margin-bottom: 0.3rem; color: #444; }
.field input {
  padding: 0.55rem 0.75rem;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}
.field input:focus { border-color: #42b883; }
button {
  width: 100%;
  padding: 0.65rem;
  background: #42b883;
  color: #fff;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: background 0.2s;
}
button:disabled { background: #aaa; cursor: not-allowed; }
button:hover:not(:disabled) { background: #369f6e; }
.error { color: #e74c3c; font-size: 0.85rem; margin: 0.5rem 0; }
.toggle { text-align: center; margin-top: 1.25rem; font-size: 0.9rem; color: #666; }
.toggle a { color: #42b883; text-decoration: none; font-weight: 500; }
</style>

