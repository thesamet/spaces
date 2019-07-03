package spaces.services

import cats.effect.IO
import spaces.api.protos.{CreateBusinessRequest, CreateBusinessResponse}

trait BusinessService {
  def createNewBusiness(
      createBusinessRequest: CreateBusinessRequest): IO[CreateBusinessResponse]
}

class BusinessServiceImpl extends BusinessService {
  override def createNewBusiness(createBusinessRequest: CreateBusinessRequest)
    : IO[CreateBusinessResponse] = ???
}
