import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="hero bg-base-100 rounded-box shadow-md my-8 p-8">
      <div className="hero-content text-center">
        <div>
          <h2 className="text-4xl font-bold text-error mb-2">404</h2>
          <p className="text-lg mb-4">Az oldal nem található!</p>
          <Link to="/" className="btn btn-outline py-2 px-4">
            Vissza a kezdőlapra
          </Link>
        </div>
      </div>
    </div>
  )
}
