import { PROJECT_NAME } from '../utils/constants'

function Home() {
  return (
    <section className="mx-auto flex min-h-[70vh] w-full max-w-5xl flex-col justify-center px-6 py-16">
      <div className="max-w-3xl">
        <p className="mb-4 text-sm font-semibold uppercase tracking-wider text-autohall">
        </p>
        <h1 className="text-4xl font-bold tracking-tight text-slate-950 sm:text-5xl">
          {PROJECT_NAME}
        </h1>
        <p className="mt-5 text-lg text-slate-600">
        
        </p>
      </div>
    </section>
  )
}

export default Home
