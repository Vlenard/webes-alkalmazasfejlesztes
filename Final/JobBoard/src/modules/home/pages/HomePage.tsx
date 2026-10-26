import { Link } from 'react-router-dom'

export default function HomePage() {
  return (
    <div className="hero bg-base-100 rounded-box shadow-md my-8 p-8">
      <div className="hero-content text-center">
        <div className="max-w-md">
          <h1 className="text-4xl font-bold">Üdvözöllek a JobBoard-on!</h1>
          <p className="py-6 text-base-content/80">
            Találd meg a számodra legmegfelelőbb pozíciót a legfrissebb állásajánlataink között.
          </p>
          <Link to="/jobs" className="btn btn-primary">
            Állások böngészése
          </Link>
        </div>
      </div>
    </div>
  )
}
