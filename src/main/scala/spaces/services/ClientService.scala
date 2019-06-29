package spaces.services

import java.util.{Base64, UUID}

import spaces.api.protos._
import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import spaces.Client

trait ClientService {
  def createNewClient(createClientRequest: CreateClientRequest): IO[String]
}

class ClientServiceImpl() extends ClientService {

  override def createNewClient(
      createClientRequest: CreateClientRequest): IO[String] = {
    println(createClientRequest)
    val randomClientId = UUID.randomUUID().toString
    val newClient = Client(randomClientId,
                           createClientRequest.clientName,
                           createClientRequest.clientAgency,
                           Seq.empty,
                           Seq.empty,
                           Seq.empty)
    //store this newClient in DynamoDB
    println(newClient)
    IO { newClient.id }
  }
}
