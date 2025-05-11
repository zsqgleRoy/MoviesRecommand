// types/movie.ts
export interface Movie {
    id: number
    title: string
    poster: string
    genres: string[]
    year: number
    rating: number
  }
  
  export interface Recommendation {
    movieId: number
    title: string
    cover: string
    genres: string
    year: number
    score: number
  }