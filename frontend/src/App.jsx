import { useEffect, useState } from 'react'
import api from './api/axiosConfig'
import PageCard from './components/PageCard'
import CreatePageDrawer from './components/CreatePageDrawer'

function App() {
  const [pages, setPages] = useState([])
  const [loading, setLoading] = useState(true)
  const [isDrawerOpen, setIsDrawerOpen] = useState(false)

  const fetchPages = () => {
    api.get('/landing-pages')
      .then(res => {
        setPages(res.data)
        setLoading(false)
      })
      .catch(err => {
        console.error("Erreur de connexion :", err)
        setLoading(false)
      })
  }

  useEffect(() => {
    fetchPages()
  }, [])

  const handlePageCreated = () => {
    fetchPages()
  }

  return (
    <div className="min-h-screen bg-[#0a0a0a] p-8">
      {/* Header */}
      <div className="max-w-7xl mx-auto mb-12 flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-black text-white mb-2 tracking-tight">
            Auto Hall
          </h1>
          <p className="text-slate-400 text-lg font-medium">
            Construction de Landing Pages • {pages.length} page{pages.length !== 1 ? 's' : ''} créée{pages.length !== 1 ? 's' : ''}
          </p>
        </div>
        <button
          onClick={() => setIsDrawerOpen(true)}
          className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-500 text-white font-semibold rounded-xl transition-all shadow-lg hover:shadow-blue-500/25"
        >
          <span className="text-xl">+</span>
          NEW PAGE
        </button>
      </div>

      {/* Contenu principal */}
      {loading ? (
        <div className="flex items-center justify-center min-h-[60vh]">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin mx-auto mb-4" />
            <p className="text-slate-400 text-sm">Chargement des landing pages...</p>
          </div>
        </div>
      ) : pages.length === 0 ? (
        <div className="flex items-center justify-center min-h-[60vh]">
          <div className="text-center p-12 bg-white/5 rounded-3xl border border-white/10 max-w-md">
            <p className="text-slate-300 text-xl font-medium mb-2">Aucune landing page</p>
            <p className="text-slate-500">Créez votre première page pour commencer.</p>
          </div>
        </div>
      ) : (
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
            {pages.map(page => (
              <PageCard key={page.id} page={page} />
            ))}
          </div>
        </div>
      )}

      {/* Create Page Drawer */}
      <CreatePageDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onPageCreated={handlePageCreated}
      />
    </div>
  )
}

export default App