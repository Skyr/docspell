package docspell.joex.process

import docspell.common._
import docspell.joex.process.ItemData.AttachmentDates
import docspell.store.records.{RAttachment, RAttachmentMeta, RItem}

/** Data that is carried across all processing tasks.
  *
  * @param item the stored item record
  * @param attachments the attachments belonging to the item
  * @param metas the meta data to each attachment; depending on the
  * state of processing, this may be empty
  * @param dateLabels a separate list of found dates
  * @param originFile a mapping from an attachment id to a filemeta-id
  * containng the source or origin file
  * @param givenMeta meta data to this item that was not "guessed"
  * from an attachment but given and thus is always correct
  */
case class ItemData(
    item: RItem,
    attachments: Vector[RAttachment],
    metas: Vector[RAttachmentMeta],
    dateLabels: Vector[AttachmentDates],
    originFile: Map[Ident, Ident], // maps RAttachment.id -> FileMeta.id
    givenMeta: MetaProposalList    // given meta data not associated to a specific attachment
) {

  def findMeta(attachId: Ident): Option[RAttachmentMeta] =
    metas.find(_.id == attachId)

  def findDates(rm: RAttachmentMeta): Vector[NerDateLabel] =
    dateLabels.find(m => m.rm.id == rm.id).map(_.dates).getOrElse(Vector.empty)

  def mapMeta(attachId: Ident, f: RAttachmentMeta => RAttachmentMeta): ItemData = {
    val item = changeMeta(attachId, f)
    val next = metas.map(a => if (a.id == attachId) item else a)
    copy(metas = next)
  }

  def changeMeta(
      attachId: Ident,
      f: RAttachmentMeta => RAttachmentMeta
  ): RAttachmentMeta =
    f(findOrCreate(attachId))

  def findOrCreate(attachId: Ident): RAttachmentMeta =
    metas.find(_.id == attachId).getOrElse {
      RAttachmentMeta.empty(attachId)
    }

}

object ItemData {

  case class AttachmentDates(rm: RAttachmentMeta, dates: Vector[NerDateLabel]) {
    def toNerLabel: Vector[NerLabel] =
      dates.map(dl => dl.label.copy(label = dl.date.toString))
  }

}
