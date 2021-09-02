package boundary;

import control.exception.AlreadyExistingException;
import entity.ActorMovieEntity;
import entity.Movie;
import entity.dto.ActorDTO;
import entity.dto.MovieDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import java.util.List;

import static io.quarkus.hibernate.reactive.panache.Panache.withTransaction;

@GraphQLApi
public class MovieResource {

    @Query("allMovies")
    @Description("Get all Movies")
    public Uni<List<MovieDTO>> getAllMovies() {
        return withTransaction(() -> Movie.getAllMovies().onItem().transform(MovieDTO::from));
    }

    @Query
    @Description("Get a movie")
    public Uni<MovieDTO> getMovie(@Name("movieId") long id) {
        return withTransaction(() -> Movie.findByMovieId(id).onItem().transform(MovieDTO::from));
    }

    public Uni<List<ActorDTO>> actors(@Source(name = "MovieResponse") MovieDTO movie) {
        return withTransaction(() ->
                ActorMovieEntity.getActorsByMovieQuery(movie.id)
                .onItem()
                .transform(actorMovieEntity ->
                        ActorDTO.from(actorMovieEntity.actor))
                .collect().asList());
    }

    @Mutation
    @Description("Create a movie")
    public Uni<MovieDTO> createMovie(Movie movie) {
        return withTransaction(() -> Movie.addMovie(movie).onItem().transform(MovieDTO::from));
    }

    @Mutation
    @Description("Update a movie")
    public Uni<MovieDTO> updateMovie(@Name("movieId") long id, Movie movie) {
        return withTransaction(() -> Movie.updateMovie(id, movie).onItem().transform(MovieDTO::from));
    }

    @Mutation
    @Description("Delete a movie")
    public Uni<Boolean> deleteMovie(@Name("movieId") long id) {
        return withTransaction(() -> Movie.deleteMovie(id));
    }

    @Mutation
    @Description("Add actor to movie")
    public Uni<MovieDTO> addActorToMovie(@Name("movieId") long movieId, @Name("actorId") long actorId) {
        return withTransaction(() ->
                Movie.addActorToMovie(movieId, actorId).onItem().transform(MovieDTO::from).onFailure().
                transform(throwable -> new AlreadyExistingException("movieId: " + movieId + " and actorId: " + actorId)));
    }

}