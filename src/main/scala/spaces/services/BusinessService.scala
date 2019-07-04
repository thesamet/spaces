package spaces.services

import java.util.UUID

import cats.effect.IO
import spaces.Business
import spaces.api.protos.{CreateBusinessRequest, CreateBusinessResponse, GetBusinessRequest, GetBusinessResponse}

trait BusinessService {
  def getBusiness(req: GetBusinessRequest) : IO[GetBusinessResponse]

  def createNewBusiness(
      createBusinessRequest: CreateBusinessRequest): IO[CreateBusinessResponse]
}

class BusinessServiceImpl extends BusinessService {
  override def createNewBusiness(createBusinessRequest: CreateBusinessRequest)
    : IO[CreateBusinessResponse] = {
    val businessId = UUID.randomUUID().toString
    val newBusiness = Business(
      id = businessId,
      name = createBusinessRequest.name,
      address = createBusinessRequest.address.getOrElse("Default Address"),
      phone =
        createBusinessRequest.phoneNumber.getOrElse("Default Phone Number"),
      lat = createBusinessRequest.lat,
      long = createBusinessRequest.long,
      timezone = createBusinessRequest.timezone,
      weeklyHours = Seq.empty, //TODO write converters for these classes.
      dynamicActionButton = Seq.empty
    )
    val clientId = createBusinessRequest.clientId
    println(newBusiness)
    //Store new Business in DDB and associate the client with the passed on client ID
    IO.pure(CreateBusinessResponse(newBusiness.id))
  }

  //get Business from DDB or raise error if not found
  override def getBusiness(req: GetBusinessRequest): IO[GetBusinessResponse] = ???
}
