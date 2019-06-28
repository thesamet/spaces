package spaces.services



import java.util.{Base64, UUID}

import spaces.Client
import cats.data.OptionT
import cats.effect.IO
import cats.implicits._



trait ClientService {
  def createNewClient(client: Client) : IO[String] = IO { "this should never be called" }
}

class ClientServiceImpl() extends ClientService {

  override def createNewClient(client: Client): IO[String] = {
    val randomClientId = UUID.randomUUID().toString
    val newClient = Client(randomClientId, "pizzahut", "marchex", Seq.empty, Seq.empty, Seq.empty)
    //store this newClient in DynamoDB
    IO{ newClient.toProtoString }
  }
}
