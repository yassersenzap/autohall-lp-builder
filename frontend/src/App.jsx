import { useEffect, useState } from 'react'
import api from './api/axiosConfig'

function App() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/landing-pages')
      .then(res => {
        setData(res.data)
        setLoading(false)
      })
      .catch(err => {
        console.error("Erreur de connexion :", err)
        setLoading(false)
      })
  }, [])

  return (
    <div className="min-h-screen bg-[#0f172a] flex items-center justify-center p-6 text-slate-800">
      <div className="bg-white p-8 rounded-3xl shadow-2xl border-t-4 border-blue-600 max-w-sm w-full text-center">
        <h1 className="text-3xl font-black mb-2">Auto Hall</h1>
        <p className="text-slate-500 font-medium italic mb-6">Status de la liaison</p>
        
        <div className={`py-2 px-4 rounded-full font-bold text-sm inline-block mb-4 ${data ? 'bg-green-100 text-green-700' : 'bg-amber-100 text-amber-700'}`}>
          {loading ? "Connexion en cours..." : data ? "Backend Connecté ✅" : "Vérifie ton Java ⚠️"}
        </div>

        <div className="text-left bg-slate-50 p-4 rounded-xl font-mono text-xs text-slate-400 overflow-auto max-h-32">
          {data ? JSON.stringify(data) : "En attente de données..."}
        </div>
      </div>
    </div>
  )
}

export default App