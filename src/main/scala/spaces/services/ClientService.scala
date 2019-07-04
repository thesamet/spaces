package spaces.services

import java.util.{Base64, UUID}

import spaces.api.protos._
import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import spaces.Client

trait ClientService {
  def getClient(req: GetClientRequest) : IO[GetClientResponse]

  def createNewClient(
      createClientRequest: CreateClientRequest): IO[CreateClientResponse]
}

class ClientServiceImpl() extends ClientService {

  override def createNewClient(
      createClientRequest: CreateClientRequest): IO[CreateClientResponse] = {
    println(createClientRequest)
    val randomClientId = UUID.randomUUID().toString
    val newClient = Client(randomClientId,
                           createClientRequest.name,
                           createClientRequest.agency,
                           Seq.empty,
                           Seq.empty,
                           Seq.empty)
    //store this newClient in DynamoDB
    println(newClient)
    IO.pure( CreateClientResponse(newClient.id) )
  }

  //retrieve client data from DB. Raise an IO error if not found
  override def getClient(req: GetClientRequest): IO[GetClientResponse] = ???
}
