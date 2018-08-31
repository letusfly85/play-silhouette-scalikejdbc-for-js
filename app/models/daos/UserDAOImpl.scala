package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.{ AuthUser, Users }
import models.daos.UserDAOImpl._

import scala.collection.mutable
import scala.concurrent.Future

/**
 * Give access to the user object.
 */
class UserDAOImpl extends UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) =
    Future.successful(
      users.find { case (_, user) => user.loginInfo == loginInfo }.map(_._2)
    )

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = Future.successful(users.get(userID))

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: AuthUser) = {
    users += (user.userID -> user)
    Future.successful(user)
  }
}

/**
 * The companion object.
 */
object UserDAOImpl {

  lazy val users: mutable.HashMap[UUID, AuthUser] = convertSilhouetteUser(Users.findAll())

  def convertSilhouetteUser(modelUsers: List[Users]): mutable.HashMap[UUID, AuthUser] = {
    val silhouetteUsers = modelUsers.map { user =>
      val uuid = java.util.UUID.fromString(user.userId)
      val loginInfo = LoginInfo(providerID = "credentials", providerKey = user.email)

      new AuthUser(
        userID = uuid,
        loginInfo = loginInfo,
        role = user.role,
        firstName = user.firstName,
        lastName = user.firstName,
        fullName = Some(user.firstName.getOrElse("") + user.lastName.getOrElse("")),
        email = Some(user.email),
        avatarURL = user.avatarUrl,
        activated = user.activated.getOrElse(false)
      )
    }

    val userMap: mutable.HashMap[UUID, AuthUser] = mutable.HashMap[UUID, AuthUser]()
    silhouetteUsers.map(su => userMap.put(su.userID, su))
    userMap
  }
}
