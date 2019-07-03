package spaces.api

import cats.effect.IO
import cats.implicits._
import io.circe.Json
import org.http4s.{AuthedRequest, AuthedService, Response}
import spaces.api.protos._
import spaces.auth.User
import spaces.services._

private class Api(workspaceService: WorkspaceService,
                  idService: IdService,
                  directoryService: DirectoryService,
                  clientService: ClientService,
                  businessService: BusinessService,
                  campaignService: CampaignService) {
  import org.http4s.dsl.io._
  import JsonProtoUtils._

  val requestHandler
    : PartialFunction[AuthedRequest[IO, User], IO[Response[IO]]] = {
    case req @ POST -> Root / "createWorkspace" as user =>
      for {
        req <- req.req
          .as[CreateWorkspaceRequest]
          .ensure(new InvalidRequest("Missing groups"))(_.groupRefs.nonEmpty)
        wsId <- idService.newId[Workspace]
        workspace <- workspaceService.newWorkspace(wsId,
                                                   req.name,
                                                   req.groupRefs)
        r <- Ok(workspace)
      } yield r

      //start NEW CODE
    case req @ POST -> Root / "createClient" as user => {
      for {
        req <- req.req
          .as[CreateClientRequest]
        resp <- Ok(clientService.createNewClient(req))
      } yield resp
    }

    case req @ POST -> Root / "createBusiness" as user => {
      for {
        req <- req.req
          .as[CreateBusinessRequest]
        resp <- Ok(businessService.createNewBusiness(req))
      } yield resp
    }

    case req @ POST -> Root / "deleteWorkspace" as user =>
      for {
        req <- req.req.as[DeleteWorkspaceRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        ws <- Ok(workspaceService.deleteWorkspace(workspace))
      } yield ws

    case req @ POST -> Root / "createEnvironment" as user =>
      for {
        req <- req.req.as[CreateEnvironmentRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        envId <- idService.newId[Environment]
        env <- workspaceService.addEnvironment(workspace, envId, req.name)
        r <- Ok(env)
      } yield r

    case req @ POST -> Root / "deleteEnvironment" as user =>
      for {
        req <- req.req.as[DeleteEnvironmentRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        ws <- Ok(
          workspaceService.deleteEnvironment(workspace, req.environmentId))
      } yield ws

    case req @ POST -> Root / "linkSourceRepository" as user =>
      for {
        req <- req.req.as[LinkSourceRepositoryRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        ws <- workspaceService.linkSourceRepository(workspace, req.ref)
        r <- Ok(ws)
      } yield r

    case req @ POST -> Root / "unlinkSourceRepository" as user =>
      for {
        req <- req.req.as[LinkSourceRepositoryRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        ws <- workspaceService.unlinkSourceRepository(workspace, req.ref)
        r <- Ok(ws)
      } yield r

    case req @ POST -> Root / "linkDatabase" as user =>
      for {
        req <- req.req.as[LinkDatabaseRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        ws <- workspaceService.linkDatabase(workspace,
                                            req.environmentId,
                                            req.ref)
        r <- Ok(ws)
      } yield r

    case req @ POST -> Root / "unlinkDatabase" as user =>
      for {
        req <- req.req.as[LinkDatabaseRequest]
        workspace <- workspaceService.getWorkspace(req.workspaceId, user)
        ws <- workspaceService.unlinkDatabase(workspace,
                                              req.environmentId,
                                              req.ref)
        r <- Ok(ws)
      } yield r

    case req @ GET -> Root / "workspaces" as user =>
      import org.http4s.circe._
      for {
        wss <- workspaceService.listWorkspaces(user)
        res <- Ok(Json.arr(wss.map(id => Json.fromLong(id.id)): _*))
      } yield res

    case req @ GET -> Root / "workspaces" / WorkspaceIdVar(workspaceId) as user =>
      for {
        ws <- workspaceService.getWorkspace(workspaceId, user)
        // Get all groups in parallel
        groups <- ws.groupIds.toVector.parFlatTraverse { t =>
          directoryService.getGroup(t).map(_.toVector)
        }
        res <- Ok(ws.withOwners(groups.map(grp =>
          grp.withUsers(grp.users.map(_.clearGroupIds)))))
      } yield res

    case req @ GET -> Root / "workspaces" / WorkspaceIdVar(workspaceId) / "environments" / EnvironmentIdVar(
          envId) as user =>
      for {
        ws <- workspaceService.getWorkspace(workspaceId, user)
        env <- IO.fromEither(
          ws.environments
            .find(_.id == envId)
            .toRight(ObjectNotFound("Environment not found")))
        res <- Ok(env)
      } yield res

    case req @ GET -> "workspaces" /: WorkspaceIdVar(workspaceId) /: "environments" /: EnvironmentIdVar(
          envId) /: "databases" /: dbRef as user =>
      val dbRefStr = dbRef.toString.stripPrefix("/")
      for {
        ws <- workspaceService.getWorkspace(workspaceId, user)
        env <- IO.fromEither(
          ws.environments
            .find(_.id == envId)
            .toRight(ObjectNotFound("Environment not found")))
        db <- IO.fromEither(
          env.databases
            .find(_.ref == dbRefStr)
            .toRight(ObjectNotFound("Database not found")))
        res <- Ok(db)
      } yield res

    case req @ GET -> "workspaces" /: WorkspaceIdVar(workspaceId) /: "repositories" /: repoRef as user =>
      val repoRefStr = repoRef.toString.stripPrefix("/")
      for {
        ws <- workspaceService.getWorkspace(workspaceId, user)
        repo <- IO.fromEither(
          ws.repositories
            .find(_.ref == repoRefStr)
            .toRight(ObjectNotFound("Repository not found")))
        res <- Ok(repo)
      } yield res
  }

  def errorHandler(resp: IO[Response[IO]]) =
    resp.handleErrorWith {
      case e: ApiError =>
        e match {
          case NoAccess(msg)              => Forbidden(msg)
          case InvalidRequest(msg)        => BadRequest(msg)
          case ObjectNotFound(msg)        => NotFound(msg)
          case DataConflictException(msg) => Conflict(msg)
          case DuplicateName(msg)         => BadRequest(msg)
        }
      case e => IO.raiseError(e)
    }

  val service: AuthedService[User, IO] =
    AuthedService[User, IO](requestHandler andThen errorHandler)
}

object Api {
  def buildService(
      workspaceService: WorkspaceService,
      idService: IdService,
      directoryService: DirectoryService,
      clientService: ClientService,
      businessService: BusinessService,
      campaignService: CampaignService): AuthedService[User, IO] = {
    new Api(workspaceService, idService, directoryService, clientService, businessService, campaignService).service
  }
}
