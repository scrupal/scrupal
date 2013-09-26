/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.utils

import play.api.templates.Html

/**
 * Enumeration of icons available in Scrupal.
 * This list of icons are scalable as a vector font and provided by font-awesome
 */

object Icons extends Enumeration
{
  type Icons = Value
  val adjust = Value("adjust")
  val adn = Value("adn")
  val align_center = Value("align_center")
  val align_justify, align_left, align_right,	ambulance, anchor, android, angle_down,
  angle_left, angle_right, angle_up, apple, archive,  arrow_down, arrow_left,  arrow_right, arrow_up,  asterisk,
  backward, ban_circle, bar_chart, barcode, beaker, beer, bell, bell_alt, bitbucket, bitbucket_sign, bold, bolt,
  book, bookmark, bookmark_empty, briefcase, btc, bug, building, bullhorn, bullseye,
  calendar, calendar_empty, camera, camera_retro, caret_down, caret_left, caret_right, caret_up, certificate, check,
  check_empty, check_minus, check_sign, chevron_down, chevron_left, chevron_right, chevron_sign_down,
  chevron_sign_left, chevron_sign_right, chevron_sign_up, chevron_up, circle, circle_arrow_down, circle_arrow_left,
  circle_arrow_right, circle_arrow_up, circle_blank, cloud, cloud_download, cloud_upload, cny, code, code_fork,
  coffee, cog, cogs, collapse, collapse_alt, collapse_top, columns, comment, comment_alt, comments, comments_alt,
  compass, copy, credit_card, crop, css3, cut,
  dashboard, desktop, double_angle_down, double_angle_left, double_angle_right, double_angle_up, download,
  download_alt, dribbble, dropbox,
  edit, edit_sign, eject, ellipsis_horizontal, ellipsis_vertical, envelope, envelope_alt, eraser, eur, exchange,
  exclamation,  exclamation_sign, expand, expand_alt, external_link, external_link_sign, eye_close, eye_open,
  facebook, facebook_sign, facetime_video, fast_backward, fast_forward, female, fighter_jet, file, file_alt, file_text,
  file_text_alt, film, filter, fire, fire_extinguisher, flag, flag_alt, flag_checkered, flickr, folder_close,
  folder_close_alt, folder_open, folder_open_alt, font, food, forward, foursquare, frown, fullscreen,
  gamepad, gbp, gift, github, github_alt, github_sign, gittip, glass, globe, google_plus, google_plus_sign, group,
  h_sign, hand_down, hand_left, hand_right, hand_up, hdd, headphones, heart, heart_empty, home, hospital, html5,
  inbox, indent_left, indent_right, info, info_sign, inr, instagram, italic, jpy, key, keyboard, krw, laptop, leaf,
  legal,  lemon, level_down, level_up, lightbulb, link, linkedin, linkedin_sign, linux, list, list_alt, list_ol,
  list_ul, location_arrow, lock, long_arrow_down, long_arrow_left, long_arrow_right, long_arrow_up,
  magic, magnet, mail_reply_all, male, map_marker, maxcdn, medkit, meh, microphone, microphone_off, minus, minus_sign,
  minus_sign_alt, mobile_phone, money, moon, move, music,
  off, ok, ok_circle, ok_sign,
  paper_clip, paste, pause, pencil, phone, phone_sign, picture, pinterest, pinterest_sign, plane, play, play_circle,
  play_sign, plus, plus_sign, plus_sign_alt, print, pushpin, puzzle_piece,
  qrcode, question, question_sign, quote_left, quote_right,
  random, refresh, remove, remove_circle, remove_sign, renren, reorder, repeat, reply, reply_all, resize_full,
  resize_horizontal, resize_small, resize_vertical, retweet, road, rocket, rss, rss_sign,
  save, screenshot, search, share, share_alt, share_sign, shield, shopping_cart, sign_blank, signal, signin, signout,
  sitemap, skype, smile, sort, sort_by_alphabet, sort_by_alphabet_alt, sort_by_attributes, sort_by_attributes_alt,
  sort_by_order, sort_by_order_alt, sort_down, sort_up, spinner, stackexchange, star, star_empty, star_half,
  star_half_empty, step_backward, step_forward, stethoscope, stop, strikethrough, subscript, suitcase, sun,
  superscript,
  table, tablet, tag, tags, tasks, terminal, text_height, text_width, th, th_large, th_list, thumbs_down,
  thumbs_down_alt, thumbs_up, thumbs_up_alt, ticket, time, tint, trash, trello, trophy, truck, tumblr, tumblr_sign,
  twitter, twitter_sign,
  umbrella, underline, undo, unlink, unlock, unlock_alt, upload, upload_alt, usd, user, user_md,
  vk, volume_down, volume_off, volume_up,
  warning_sign, weibo, windows, wrench,
  xing, xing_sign,
  youtube, youtube_play, youtube_sign,
  zoom_in, zoom_out
  = Value;

  def name(kind : Icons) : String = {
    "icon-" + kind.toString.replace('_','-')
  }

  def html(kind: Icons) : Html = Html("<i class=\"" + name(kind) + "\"></i>")

  /*
  implicit val iconsReader : Reads[Icons] = new Reads[Icons] {
    def reads(jsValue: JsValue) : json.JsResult[Icons] = {
      (jsValue \ "alert_kind").validate[String].map { s => Icons.withName(s) }
    }
  }

  implicit val iconsWriter : Writes[Icons] = new Writes[Icons] {
    def writes(alert: Icons) : JsValue = JsString(alert.toString)
  }

  implicit val consFormatter : Format[Icons] = Format(iconsReader, iconsWriter)
  */
}
