package spaces.services



import java.util.{Base64, UUID}

import spaces.Client
import cats.data.OptionT
import cats.effect.IO
import cats.implicits._



trait ClientService {
  def createNewClient(clientName: String, clientAgency: String) : IO[String] = IO { "this should never be called" }
}

class ClientServiceImpl() extends ClientService {

  override def createNewClient(clientName: String, clientAgency: String): IO[String] = {
    val randomClientId = UUID.randomUUID().toString
    val newClient = Client(randomClientId, clientName, clientAgency, Seq.empty, Seq.empty, Seq.empty)
    //store this newClient in DynamoDB
    println(newClient)
    IO{ newClient.id }
  }
}
