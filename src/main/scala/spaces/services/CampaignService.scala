package spaces.services

import cats.effect.IO
import spaces.api.protos.{
  CreateCampaignRequest,
  CreateCampaignResponse,
  CreateCampaignWindowRequest,
  CreateCampaignWindowResponse
}

trait CampaignService {
  def createNewCampaign(
      createCampaignRequest: CreateCampaignRequest): IO[CreateCampaignResponse]

  def createNewCampaignWindow(
      createCampaignWindowRequest: CreateCampaignWindowRequest)
    : IO[CreateCampaignWindowResponse]
}

class CampaignServiceImpl extends CampaignService {
  override def createNewCampaign(createCampaignRequest: CreateCampaignRequest)
    : IO[CreateCampaignResponse] = ???

  override def createNewCampaignWindow(
      createCampaignWindowRequest: CreateCampaignWindowRequest)
    : IO[CreateCampaignWindowResponse] = ???
}
