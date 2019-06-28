package spaces

import scalapb.TypeMapper

/** To increase type-safety in our implementation, we wrap all ids in a value class, and thus
  * make distinct types for them. Our goal is to prevent mixing up id types at compile time.
  * By creating a specific type of each id, a call to a function that accepts a Id[Workspace]
  * will not compile if it is given an Id[Environment].
  */
case class Id[A](id: Long)

// To make it a little more interesting, directory service Ids are strings and not longs...
sealed trait DirectoryServiceId extends Any {
  def id: String
}

case class UserId(id: String) extends AnyVal with DirectoryServiceId

case class UserGroupId(id: String) extends AnyVal with DirectoryServiceId

object UserId {
  implicit val userIdMapper = TypeMapper(UserId(_))(_.id)
}

object UserGroupId {
  implicit val userGroupIdMapper = TypeMapper(UserGroupId(_))(_.id)
}

object Id {
  // For ScalaPB, define bi-directional mappings from the wrapper types to the
  // underlying primitives.
  implicit def typeMapper[A] = TypeMapper(Id[A](_))(_.id)
}
