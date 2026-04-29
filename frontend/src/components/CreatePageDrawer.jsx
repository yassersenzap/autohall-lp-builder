import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { X, Plus } from 'lucide-react'
import api from '../api/axiosConfig'

const CreatePageDrawer = ({ isOpen, onClose, onPageCreated }) => {
  const [formData, setFormData] = useState({
    title: '',
    slug: '',
    hero_image: '',
    description: ''
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError(null)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)

    try {
      const payload = {
        title: formData.title,
        slug: formData.slug,
        content: {
          hero_image: formData.hero_image
        },
        description: formData.description || undefined
      }

      const response = await api.post('/landing-pages', payload)
      onPageCreated(response.data)
      setFormData({ title: '', slug: '', hero_image: '', description: '' })
      onClose()
    } catch (err) {
      setError(err.response?.data?.message || err.message || "Erreur lors de la création")
    } finally {
      setSaving(false)
    }
  }

  const drawerVariants = {
    closed: { x: '100%', opacity: 0 },
    open: { x: 0, opacity: 1 }
  }

  const overlayVariants = {
    closed: { opacity: 0 },
    open: { opacity: 1 }
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Overlay */}
          <motion.div
            className="fixed inset-0 bg-black/70 backdrop-blur-sm z-40"
            variants={overlayVariants}
            initial="closed"
            animate="open"
            exit="closed"
            onClick={onClose}
          />

          {/* Drawer */}
          <motion.div
            className="fixed top-0 right-0 h-full w-full max-w-md bg-slate-950 shadow-2xl z-50 flex flex-col border-l border-white/10"
            variants={drawerVariants}
            initial="closed"
            animate="open"
            exit="closed"
            transition={{ type: 'tween', duration: 0.3, ease: 'easeOut' }}
          >
            {/* Header */}
            <div className="p-6 border-b border-white/10 flex items-center justify-between">
              <h2 className="text-xl font-bold text-white tracking-tight">
                Nouvelle Page
              </h2>
              <button
                onClick={onClose}
                className="p-2 hover:bg-white/10 rounded-full transition-colors"
                aria-label="Fermer"
              >
                <X size={20} className="text-slate-400" />
              </button>
            </div>

            {/* Formulaire */}
            <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-6">
              {error && (
                <div className="p-4 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
                  {error}
                </div>
              )}

              {/* Title */}
              <div>
                <label htmlFor="title" className="block text-sm font-medium text-slate-300 mb-2">
                  Titre <span className="text-red-400">*</span>
                </label>
                <input
                  type="text"
                  id="title"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 bg-slate-900 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 transition-all"
                  placeholder="Ex: Ford Fiesta Promotion"
                />
              </div>

              {/* Slug */}
              <div>
                <label htmlFor="slug" className="block text-sm font-medium text-slate-300 mb-2">
                  Slug (URL) <span className="text-red-400">*</span>
                </label>
                <input
                  type="text"
                  id="slug"
                  name="slug"
                  value={formData.slug}
                  onChange={handleChange}
                  required
                  pattern="[a-z0-9-]+"
                  title="Only lowercase letters, numbers and hyphens"
                  className="w-full px-4 py-3 bg-slate-900 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 transition-all"
                  placeholder="Ex: ford-fiesta-promo"
                />
                <p className="text-xs text-slate-500 mt-1">Only lowercase, numbers, and hyphens</p>
              </div>

              {/* Hero Image URL */}
              <div>
                <label htmlFor="hero_image" className="block text-sm font-medium text-slate-300 mb-2">
                  Image URL <span className="text-red-400">*</span>
                </label>
                <input
                  type="url"
                  id="hero_image"
                  name="hero_image"
                  value={formData.hero_image}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 bg-slate-900 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 transition-all"
                  placeholder="https://example.com/image.jpg"
                />
              </div>

              {/* Description (optional) */}
              <div>
                <label htmlFor="description" className="block text-sm font-medium text-slate-300 mb-2">
                  Description
                </label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  rows={4}
                  className="w-full px-4 py-3 bg-slate-900 border border-white/10 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 transition-all resize-none"
                  placeholder="Courte description de la page (optionnel)"
                />
              </div>
            </form>

            {/* Footer boutons */}
            <div className="p-6 border-t border-white/10 flex gap-3">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 px-6 py-3 rounded-xl border border-white/20 text-slate-300 font-medium hover:bg-white/5 transition-colors"
              >
                Annuler
              </button>
              <button
                type="submit"
                onClick={handleSubmit}
                disabled={saving}
                className="flex-[2] px-6 py-3 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
              >
                {saving ? (
                  <>
                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Enregistrement...
                  </>
                ) : (
                  <>
                    <Plus size={18} />
                    Enregistrer
                  </>
                )}
              </button>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}

export default CreatePageDrawer
