<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'

const router = useRouter()
const auth = useAuthStore()

// State
const pessoas = ref([])
const totalPages = ref(0)
const currentPage = ref(0)
const pageSize = 10
const loading = ref(false)
const error = ref('')

// Modal
const showModal = ref(false)
const editMode = ref(false)
const form = ref({ id: null, name: '', email: '', birthDate: '' })
const formError = ref('')
const saving = ref(false)

async function fetchPessoas(page = 0) {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get('/pessoas', { params: { page, size: pageSize } })
    pessoas.value = data.content
    totalPages.value = data.totalPages
    currentPage.value = data.number
  } catch (e) {
    error.value = 'Erro ao carregar lista de pessoas.'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editMode.value = false
  form.value = { id: null, nome: '', email: '', dataNascimento: '' }
  formError.value = ''
  showModal.value = true
}

function openEdit(pessoa) {
  editMode.value = true
  form.value = { ...pessoa }
  formError.value = ''
  showModal.value = true
}

async function save() {
  formError.value = ''
  saving.value = true
  try {
    if (editMode.value) {
      await api.put(`/pessoa/${form.value.id}`, form.value)
    } else {
      await api.post('/pessoa', form.value)
    }
    showModal.value = false
    await fetchPessoas(currentPage.value)
  } catch (e) {
    const msg = e.response?.data?.message || Object.values(e.response?.data?.errors || {})[0]
    formError.value = msg || 'Erro ao salvar. Verifique os dados.'
  } finally {
    saving.value = false
  }
}

async function remove(id) {
  if (!confirm('Confirma a exclusão desta pessoa?')) return
  try {
    await api.delete(`/pessoa/${id}`)
    await fetchPessoas(currentPage.value)
  } catch {
    alert('Erro ao excluir.')
  }
}

function logout() {
  auth.logout()
  router.push('/login')
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const [y, m, d] = dateStr.split('-')
  return `${d}/${m}/${y}`
}

onMounted(() => fetchPessoas())
</script>

<template>
  <div class="page">
    <header class="toolbar">
      <h1>Pessoas</h1>
      <div class="toolbar-right">
        <span class="user-name">{{ auth.user?.name }}</span>
        <button class="btn-secondary" @click="logout">Sair</button>
        <button class="btn-primary" @click="openCreate">+ Nova Pessoa</button>
      </div>
    </header>

    <div v-if="error" class="alert-error">{{ error }}</div>

    <div class="table-wrapper">
      <table v-if="!loading">
        <thead>
          <tr>
            <th>Nome</th>
            <th>E-mail</th>
            <th>Data de nascimento</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="pessoas.length === 0">
            <td colspan="4" class="empty">Nenhuma pessoa cadastrada.</td>
          </tr>
          <tr v-for="p in pessoas" :key="p.id">
            <td>{{ p.nome }}</td>
            <td>{{ p.email }}</td>
            <td>{{ formatDate(p.dataNascimento) }}</td>
            <td class="actions">
              <button class="btn-edit" @click="openEdit(p)">Editar</button>
              <button class="btn-delete" @click="remove(p.id)">Excluir</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="loading">Carregando...</p>
    </div>

    <!-- Paginação -->
    <div v-if="totalPages > 1" class="pagination">
      <button :disabled="currentPage === 0" @click="fetchPessoas(currentPage - 1)">‹ Anterior</button>
      <span>Página {{ currentPage + 1 }} de {{ totalPages }}</span>
      <button :disabled="currentPage + 1 >= totalPages" @click="fetchPessoas(currentPage + 1)">Próxima ›</button>
    </div>

    <!-- Modal Criar / Editar -->
    <div v-if="showModal" class="modal-backdrop" @click.self="showModal = false">
      <div class="modal">
        <h2>{{ editMode ? 'Editar Pessoa' : 'Nova Pessoa' }}</h2>
        <form @submit.prevent="save">
          <div class="field">
            <label>Nome</label>
            <input v-model="form.nome" type="text" required placeholder="Nome completo" />
          </div>
          <div class="field">
            <label>E-mail</label>
            <input v-model="form.email" type="email" required placeholder="email@exemplo.com" />
          </div>
          <div class="field">
            <label>Data de nascimento</label>
            <input v-model="form.dataNascimento" type="date" required />
          </div>
          <p v-if="formError" class="error">{{ formError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="showModal = false">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="saving">
              {{ saving ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: 1.5rem 1rem; }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 0.75rem; }
.toolbar h1 { margin: 0; font-size: 1.5rem; color: #213547; }
.toolbar-right { display: flex; align-items: center; gap: 0.75rem; }
.user-name { color: #555; font-size: 0.9rem; }
.table-wrapper { overflow-x: auto; background: #fff; border-radius: 8px; box-shadow: 0 1px 8px rgba(0,0,0,0.08); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 0.75rem 1rem; text-align: left; border-bottom: 1px solid #eee; font-size: 0.92rem; }
th { background: #f8f9fa; font-weight: 600; color: #444; }
tr:last-child td { border-bottom: none; }
.empty, .loading { text-align: center; color: #999; padding: 2rem; }
.actions { display: flex; gap: 0.5rem; }
.pagination { display: flex; align-items: center; justify-content: center; gap: 1rem; margin-top: 1rem; }
.pagination button { padding: 0.4rem 0.85rem; border: 1px solid #ccc; background: #fff; border-radius: 4px; cursor: pointer; }
.pagination button:disabled { opacity: 0.4; cursor: default; }
.pagination span { font-size: 0.9rem; color: #666; }
.modal-backdrop { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 8px; padding: 2rem; width: 100%; max-width: 420px; box-shadow: 0 4px 24px rgba(0,0,0,0.18); }
.modal h2 { margin: 0 0 1.25rem; font-size: 1.2rem; }
.field { display: flex; flex-direction: column; margin-bottom: 1rem; }
.field label { font-size: 0.85rem; margin-bottom: 0.3rem; color: #444; }
.field input { padding: 0.55rem 0.75rem; border: 1px solid #ccc; border-radius: 5px; font-size: 0.95rem; outline: none; transition: border-color 0.2s; }
.field input:focus { border-color: #42b883; }
.modal-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 0.5rem; }
.btn-primary { padding: 0.5rem 1.1rem; background: #42b883; color: #fff; border: none; border-radius: 5px; cursor: pointer; font-size: 0.9rem; transition: background 0.2s; }
.btn-primary:hover:not(:disabled) { background: #369f6e; }
.btn-primary:disabled { background: #aaa; cursor: not-allowed; }
.btn-secondary { padding: 0.5rem 1.1rem; background: #fff; color: #555; border: 1px solid #ccc; border-radius: 5px; cursor: pointer; font-size: 0.9rem; }
.btn-secondary:hover { background: #f5f5f5; }
.btn-edit { padding: 0.35rem 0.75rem; background: #3498db; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 0.85rem; }
.btn-edit:hover { background: #2980b9; }
.btn-delete { padding: 0.35rem 0.75rem; background: #e74c3c; color: #fff; border: none; border-radius: 4px; cursor: pointer; font-size: 0.85rem; }
.btn-delete:hover { background: #c0392b; }
.alert-error { background: #fdecea; color: #c0392b; border-radius: 5px; padding: 0.75rem 1rem; margin-bottom: 1rem; }
.error { color: #e74c3c; font-size: 0.85rem; margin: 0.25rem 0; }
</style>

