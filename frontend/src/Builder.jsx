import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Save } from 'lucide-react'
import { motion } from 'framer-motion'
import api from './api/axiosConfig'

function Builder() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [page, setPage] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  // Fetch page data on mount
  useEffect(() => {
    const fetchPage = async () => {
      try {
        setLoading(true)
        const response = await api.get(`/landing-pages/${id}`)
        setPage(response.data)
        setError(null)
      } catch (err) {
        console.error('Erreur lors du chargement:', err)
        if (err.response?.status === 404) {
          setError('Page non trouvée')
        } else {
          setError('Erreur de connexion')
        }
      } finally {
        setLoading(false)
      }
    }

    fetchPage()
  }, [id])

  const handleInputChange = (field, value) => {
    if (!page) return

    if (field === 'title') {
      setPage({ ...page, title: value })
    } else if (field === 'price' || field === 'hero_image') {
      setPage({
        ...page,
        content: { ...(page.content || {}), [field]: value }
      })
    }
  }

  const handleSave = async () => {
    if (!page) return

    console.log('[DEBUG] Attempting to save page:', {
      id: page.id,
      title: page.title,
      slug: page.slug,
      content: page.content
    })
    console.log('[DEBUG] PUT URL:', `/landing-pages/${id}`)

    try {
      setSaving(true)
      const response = await api.put(`/landing-pages/${id}`, page)
      console.log('[DEBUG] Save response:', response.data)
      alert('Page sauvegardée avec succès!')
    } catch (err) {
      console.error('[DEBUG] Save error details:', {
        status: err.response?.status,
        data: err.response?.data,
        message: err.message
      })
      alert('Erreur lors de la sauvegarde: ' + (err.response?.data?.message || err.message))
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-blue-500/30 border-t-blue-500 rounded-full animate-spin mx-auto mb-4" />
          <p className="text-slate-400 text-sm">Chargement de la page...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
        <div className="text-center p-12 bg-red-900/20 border border-red-500/30 rounded-3xl max-w-md">
          <p className="text-red-400 text-xl font-medium mb-4">{error}</p>
          <button
            onClick={() => navigate('/')}
            className="px-6 py-3 bg-blue-600 hover:bg-blue-500 text-white font-semibold rounded-xl transition-all"
          >
            Retour au Dashboard
          </button>
        </div>
      </div>
    )
  }

  if (!page) return null

  return (
    <div className="min-h-screen bg-[#0a0a0a]">
      {/* Header */}
      <motion.div
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="border-b border-white/10 bg-slate-900/30 backdrop-blur-sm sticky top-0 z-10"
      >
        <div className="max-w-7xl mx-auto p-6 flex items-center justify-between">
          <button
            onClick={() => navigate('/')}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors"
          >
            <ArrowLeft size={20} />
            <span className="font-medium">Retour au Dashboard</span>
          </button>

          <motion.button
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={handleSave}
            disabled={saving}
            className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-500 disabled:bg-blue-900 disabled:cursor-not-allowed text-white font-semibold rounded-xl transition-all shadow-lg hover:shadow-blue-500/25"
          >
            <Save size={20} />
            {saving ? 'Enregistrement...' : 'Enregistrer'}
          </motion.button>
        </div>
      </motion.div>

      {/* Split Screen Layout */}
      <div className="flex h-[calc(100vh-80px)]">
        {/* Left Panel - Editor Settings (35%) */}
        <motion.div
          initial={{ x: -20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ delay: 0.1 }}
          className="w-[35%] bg-slate-900/50 border-r border-white/10 p-8 overflow-y-auto"
        >
          <h2 className="text-2xl font-bold text-white mb-6 tracking-tight">
            Editor Settings
          </h2>

          <div className="space-y-6">
            {/* Title Input */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Title
              </label>
              <input
                type="text"
                value={page.title || ''}
                onChange={(e) => handleInputChange('title', e.target.value)}
                className="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                placeholder="Enter page title"
              />
            </div>

            {/* Price Input */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Price
              </label>
              <input
                type="text"
                value={page.content?.price || ''}
                onChange={(e) => handleInputChange('price', e.target.value)}
                className="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                placeholder="e.g. €29.999"
              />
            </div>

            {/* Hero Image URL Input */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">
                Hero Image URL
              </label>
              <textarea
                value={page.content?.hero_image || ''}
                onChange={(e) => handleInputChange('hero_image', e.target.value)}
                rows={3}
                className="w-full px-4 py-3 bg-slate-800/50 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all resize-none"
                placeholder="https://example.com/image.jpg"
              />
            </div>

            {/* Page Info */}
            <div className="pt-6 border-t border-white/10">
              <p className="text-sm text-slate-400">
                <span className="font-medium">Slug:</span> {page.slug}
              </p>
              <p className="text-sm text-slate-400 mt-1">
                <span className="font-medium">Status:</span> {page.status}
              </p>
              <p className="text-sm text-slate-400 mt-1">
                <span className="font-medium">Created:</span>{' '}
                {new Date(page.createdAt).toLocaleDateString('fr-FR')}
              </p>
              {page.updatedAt && (
                <p className="text-sm text-slate-400 mt-1">
                  <span className="font-medium">Updated:</span>{' '}
                  {new Date(page.updatedAt).toLocaleDateString('fr-FR')}
                </p>
              )}
            </div>
          </div>
        </motion.div>

        {/* Right Panel - Live Preview (65%) */}
        <motion.div
          initial={{ x: 20, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="w-[65%] bg-slate-950/50 overflow-y-auto p-12"
        >
          <div className="max-w-4xl mx-auto">
            {/* Preview Header */}
            <div className="mb-8">
              <h1 className="text-5xl font-black text-white mb-4 tracking-tight">
                {page.title || 'Untitled'}
              </h1>
              {page.description && (
                <p className="text-xl text-slate-400">{page.description}</p>
              )}
            </div>

            {/* Live Preview Container */}
            <motion.div
              key={page.content?.hero_image}
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.3 }}
              className="relative bg-gradient-to-br from-slate-900/50 to-slate-800/30 border border-white/10 rounded-3xl overflow-hidden"
            >
              {/* Car Image Display */}
              <div className="aspect-[16/9] bg-slate-900/50 flex items-center justify-center relative">
                {page.content?.hero_image ? (
                  <motion.img
                    key={page.content.hero_image}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.4 }}
                    src={page.content.hero_image}
                    alt={page.title}
                    className="w-full h-full object-contain"
                  />
                ) : (
                  <div className="text-center p-12">
                    <div className="w-24 h-24 bg-slate-800/50 rounded-full flex items-center justify-center mx-auto mb-4">
                      <span className="text-4xl">🚗</span>
                    </div>
                    <p className="text-slate-500">
                      Aucune image définie
                    </p>
                    <p className="text-slate-600 text-sm mt-2">
                      Ajoutez une URL d'image dans le panneau de gauche
                    </p>
                  </div>
                )}
              </div>

              {/* Price Badge */}
              {page.content?.price && (
                <motion.div
                  initial={{ x: 20, opacity: 0 }}
                  animate={{ x: 0, opacity: 1 }}
                  className="absolute top-6 right-6 px-6 py-3 bg-blue-600/90 backdrop-blur-sm rounded-2xl shadow-lg border border-blue-400/20"
                >
                  <p className="text-2xl font-black text-white">
                    {page.content.price}
                  </p>
                </motion.div>
              )}

              {/* Content Info Bar */}
              <div className="px-8 py-4 bg-slate-900/30 border-t border-white/5 flex items-center justify-between">
                <p className="text-sm text-slate-400">
                  <span className="font-medium">Status:</span>{' '}
                  <span className={`px-2 py-1 rounded-lg text-xs font-semibold ${
                    page.status === 'PUBLISHED'
                      ? 'bg-green-500/20 text-green-400'
                      : page.status === 'ARCHIVED'
                      ? 'bg-orange-500/20 text-orange-400'
                      : 'bg-yellow-500/20 text-yellow-400'
                  }`}>
                    {page.status}
                  </span>
                </p>
                <p className="text-sm text-slate-400">
                  Slug: <span className="font-mono text-slate-300">{page.slug}</span>
                </p>
              </div>
            </motion.div>

            {/* Additional Preview Elements */}
            <motion.div
              initial={{ y: 20, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.4 }}
              className="mt-8 p-6 bg-slate-900/30 border border-white/10 rounded-2xl"
            >
              <h3 className="text-lg font-semibold text-white mb-3">
                Métadonnées
              </h3>
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-slate-500">ID</p>
                  <p className="text-slate-300 font-mono">{page.id}</p>
                </div>
                <div>
                  <p className="text-slate-500">Description</p>
                  <p className="text-slate-300">
                    {page.description || 'Aucune description'}
                  </p>
                </div>
              </div>
            </motion.div>
          </div>
        </motion.div>
      </div>
    </div>
  )
}

export default Builder
