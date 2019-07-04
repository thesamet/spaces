package spaces.services

import java.util.UUID

import cats.effect.IO
import spaces.{Campaign, CampaignWindow}
import spaces.api.protos.{CreateCampaignRequest, CreateCampaignResponse, CreateCampaignWindowRequest, CreateCampaignWindowResponse}

trait CampaignService {
  def createNewCampaign(
      createCampaignRequest: CreateCampaignRequest): IO[CreateCampaignResponse]

  def createNewCampaignWindow(
      createCampaignWindowRequest: CreateCampaignWindowRequest)
    : IO[CreateCampaignWindowResponse]
}

class CampaignServiceImpl extends CampaignService {
  override def createNewCampaign(createCampaignRequest: CreateCampaignRequest)
    : IO[CreateCampaignResponse] = {
    val campaignId = UUID.randomUUID().toString
    val newCampaign = Campaign(
      id = campaignId,
      title = createCampaignRequest.title,
      description = createCampaignRequest.description,
      //TODO: write converters for the rest of the custom fields
      payoutDetails = Seq.empty,
    )
    val clientId = createCampaignRequest.clientId
    println(newCampaign)
    //store Campaign and associate it with clientId
    IO.pure(CreateCampaignResponse(newCampaign.id))
  }

  override def createNewCampaignWindow(
      createCampaignWindowRequest: CreateCampaignWindowRequest)
    : IO[CreateCampaignWindowResponse] = {
    val campaignWindowId = UUID.randomUUID().toString
    val newCampaignWindow = CampaignWindow(
      id = campaignWindowId,
      description = createCampaignWindowRequest.description,
      campaignId = createCampaignWindowRequest.campaignId,
      businessId = createCampaignWindowRequest.businessIds
      //TODO: implement the rest
    )
    val campaignId = createCampaignWindowRequest.campaignId
    //associate this window with specific campaign in the DDB, also with Business(es)
    IO.pure(CreateCampaignWindowResponse(newCampaignWindow.id))
  }
}
