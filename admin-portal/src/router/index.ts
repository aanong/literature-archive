import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { ElMessage } from 'element-plus'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/login',
            name: 'login',
            component: () => import('../views/LoginView.vue')
        },
        {
            path: '/',
            name: 'home',
            component: HomeView,
            meta: { requiresAuth: true, title: '总览' }
        },
        {
            path: '/books',
            name: 'book-list',
            component: () => import('../views/book/BookListView.vue'),
            meta: { requiresAuth: true, title: '书目编目' }
        },
        {
            path: '/books/new',
            name: 'book-create',
            component: () => import('../views/book/BookFormView.vue'),
            meta: { requiresAuth: true, title: '新增书目' }
        },
        {
            path: '/books/:id/edit',
            name: 'book-edit',
            component: () => import('../views/book/BookFormView.vue'),
            meta: { requiresAuth: true, title: '编辑书目' }
        },
        {
            path: '/knowledge/ingest',
            name: 'knowledge-ingest',
            component: () => import('../views/knowledge/IngestView.vue'),
            meta: { requiresAuth: true, title: '入库校验' }
        }
    ]
})

router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth) {
        const token = localStorage.getItem('token')
        if (!token) {
            next('/login')
            return
        }
        // 前端主动检查 token 是否过期
        const expireAt = localStorage.getItem('tokenExpireAt')
        if (expireAt && Date.now() > Number(expireAt)) {
            // token 已过期，清理并跳转登录
            localStorage.removeItem('token')
            localStorage.removeItem('tokenExpireAt')
            ElMessage.warning('登录已超时，请重新登录')
            next('/login')
            return
        }
        next()
    } else {
        next()
    }
})

export default router
