package molly.core

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.syntax.functor.*
import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.reactivestreams.client.MongoDatabase
import molly.core.reactivestreams.fromStreamPublisher
import molly.core.syntax.bsondocument.BsonDocumentCollection
import org.bson.BsonDocument

/** Molly's counterpart to
  * [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html MongoDatabase]]
  */
final case class MollyDatabase[F[_]] private[core] (private[core] val delegate: MongoDatabase)(using f: Async[F]):

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#getCollection(java.lang.String)]]
      */
    def getCollection(collectionName: String): F[BsonDocumentCollection[F]] =
        f.delay(delegate.getCollection(collectionName, classOf[BsonDocument])).map(MollyCollection(_))

    /** Like [[this.getCollection]], but returns a
      * [[https://typelevel.org/cats-effect/api/3.x/cats/effect/kernel/Resource.html Resource]]
      */
    def getCollectionAsResource(collectionName: String): Resource[F, BsonDocumentCollection[F]] =
        Resource.eval(getCollection(collectionName))

    /** Like [[this.getCollection]], but maps documents to type a using the given codec.
      */
    def getTypedCollection[A](collectionName: String)(using MollyCodec[F, A]): F[MollyCollection[F, A]] =
        f.delay(delegate.getCollection(collectionName, classOf[BsonDocument])).map(MollyCollection[F, A](_))

    /** Like [[this.getTypedCollection]], but returns a
      * [[https://typelevel.org/cats-effect/api/3.x/cats/effect/kernel/Resource.html Resource]]
      */
    def getTypedCollectionAsResource[A](collectionName: String)(using
        MollyCodec[F, A]
    ): Resource[F, MollyCollection[F, A]] =
        Resource.eval(getTypedCollection(collectionName))

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#listCollectionNames()]]
      */
    def listCollectionNames(): F[List[String]] = fromStreamPublisher(delegate.listCollectionNames(), 1).compile.toList

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#getReadConcern()]]
      */
    def getReadConcern(): ReadConcern = delegate.getReadConcern()

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#getReadPreference()]]
      */
    def getReadPreference(): ReadPreference = delegate.getReadPreference()

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#getWriteConcern()]]
      */
    def getWriteConcern(): WriteConcern = delegate.getWriteConcern()

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#withReadConcern(com.mongodb.ReadConcern)]]
      */
    def withReadConcern(readConcern: ReadConcern): MollyDatabase[F] =
        MollyDatabase(delegate.withReadConcern(readConcern))

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#withReadPreference(com.mongodb.ReadPreference]]
      */
    def withReadPreference(readPreference: ReadPreference): MollyDatabase[F] =
        MollyDatabase(delegate.withReadPreference(readPreference))

    /** [[https://mongodb.github.io/mongo-java-driver/5.5/apidocs/driver-reactive-streams/com/mongodb/reactivestreams/client/MongoDatabase.html#withWriteConcern(com.mongodb.WriteConcern)]]
      */
    def withWriteConcern(writeConcern: WriteConcern): MollyDatabase[F] =
        MollyDatabase(delegate.withWriteConcern(writeConcern))
