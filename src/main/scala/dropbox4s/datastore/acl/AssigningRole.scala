package dropbox4s.datastore.acl

import dropbox4s.datastore.model.TableRow

/**
 * @author mao.instantlife at gmail.com
 */
case class AssigningRole(role: Role) {
  def to(target: Principle) = TableRow(target.name, role)
}

