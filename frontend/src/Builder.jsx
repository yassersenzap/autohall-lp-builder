import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'

function Builder() {
  const { id } = useParams()
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-[#0a0a0a] p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors mb-8"
        >
          <ArrowLeft size={20} />
          <span>Retour au Dashboard</span>
        </button>

        <div className="bg-slate-900/50 border border-white/10 rounded-3xl p-12 text-center">
          <h1 className="text-4xl font-black text-white mb-4 tracking-tight">
            Builder • {id}
          </h1>
          <p className="text-slate-400 text-lg">
            L'éditeur visuel arrivera bientôt ici.
          </p>
        </div>
      </div>
    </div>
  )
}

export default Builder
