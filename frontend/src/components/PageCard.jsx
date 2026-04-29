import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { MapPin, Clock, Eye, Trash2 } from 'lucide-react';

const PageCard = ({ page, onDelete }) => {
  const navigate = useNavigate();
  const { id, title, slug, status, content } = page;

  // Extraction des données du content (peut contenir hero_image et price)
  const heroImage = content?.hero_image || '/placeholder-hero.jpg';
  const price = content?.price || null;

  const formatStatus = (status) => {
    const colors = {
      PUBLISHED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
      DRAFT: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
      ARCHIVED: 'bg-slate-500/10 text-slate-400 border-slate-500/20'
    };
    return colors[status] || 'bg-slate-500/10 text-slate-400 border-slate-500/20';
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6, ease: [0.25, 0.1, 0.25, 1] }}
      whileHover={{ y: -8 }}
      className="group relative bg-[#0a0a0a] border border-white/5 rounded-2xl overflow-hidden shadow-xl hover:shadow-2xl transition-all duration-500"
    >
      {/* Image container avec zoom au survol */}
      <div className="relative h-56 overflow-hidden bg-gradient-to-br from-slate-900 to-black">
        <img
          src={heroImage}
          alt={title}
          className="w-full h-full object-cover transform transition-transform duration-700 group-hover:scale-110 opacity-90 group-hover:opacity-100"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0a] via-transparent to-transparent opacity-80" />

        {/* Badge de statut */}
        <div className="absolute top-4 left-4">
          <span className={`px-3 py-1 text-xs font-semibold rounded-full backdrop-blur-md border ${formatStatus(status)}`}>
            {status}
          </span>
        </div>

        {/* Prix en overlay si disponible */}
        {price && (
          <div className="absolute top-4 right-4">
            <span className="px-3 py-1 text-xs font-bold bg-black/60 backdrop-blur-md text-white rounded-full border border-white/10">
              {price}
            </span>
          </div>
        )}

        {/* Action Bar - visible au survol */}
        <div className="absolute bottom-4 left-4 right-4 flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
          <button
            onClick={() => navigate(`/builder/${id}`)}
            className="p-2 bg-black/60 backdrop-blur-md rounded-full border border-white/10 text-white hover:bg-blue-600 hover:border-blue-500 transition-colors"
            aria-label="Éditer"
          >
            <Eye size={16} />
          </button>
          <button
            onClick={() => onDelete(id)}
            className="p-2 bg-black/60 backdrop-blur-md rounded-full border border-white/10 text-white hover:bg-red-600 hover:border-red-500 transition-colors"
            aria-label="Supprimer"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>

      {/* Contenu texte */}
      <div className="p-6">
        <h3 className="text-xl font-bold text-white mb-2 line-clamp-2 group-hover:text-blue-400 transition-colors duration-300">
          {title}
        </h3>

        <div className="flex items-center gap-4 text-sm text-slate-400 mb-4">
          <span className="flex items-center gap-1.5">
            <MapPin size={14} />
            {slug}
          </span>
        </div>

        {/* Footer avec ID et timestamp simulé */}
        <div className="flex items-center justify-between pt-4 border-t border-white/5 text-xs text-slate-500">
          <div className="flex items-center gap-1.5">
            <Eye size={12} />
            <span>ID: {id.slice(0, 8)}...</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Clock size={12} />
            <span>LiteRock</span>
          </div>
        </div>
      </div>
    </motion.div>
  );
};

export default PageCard;
