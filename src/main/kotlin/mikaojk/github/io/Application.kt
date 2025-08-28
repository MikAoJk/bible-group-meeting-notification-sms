package mikaojk.github.io

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import mikaojk.github.io.model.BibleGroupMeeting
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs


val log: Logger = LoggerFactory.getLogger("mikaojk.github.io")
val dateFormatt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

fun main() {
    val environment = Environment()

    val bibleGroupMeetings = fetchBibleGroupMeetingFromGoogleSheets(environment.googleSheetXlsxUrl)
    val nearestFutureBibelGroupMeeting = nearestFutureBibelGroupMeeting(bibleGroupMeetings)

    val phoneNumbers: List<String> = environment.phoneNumbers.trim().split(",")
    val allPhoneNumbersAreValid = checkPhoneNumbers(phoneNumbers)

    if (allPhoneNumbersAreValid && nearestFutureBibelGroupMeeting != null && isFutureBibelGroupMeetingNextWeek(nearestFutureBibelGroupMeeting.date)) {
        smsNotify(environment.accountSid, environment.authToken, phoneNumbers, nearestFutureBibelGroupMeeting)
    } else {
        log.info("No bible group meeting in scheduled")
    }

}

fun checkPhoneNumbers(phoneNumbers: List<String>): Boolean {
    for (phoneNumber in phoneNumbers) {
        if (!validatePhoneNumberRegex(phoneNumber)) {
            log.error("Invalid phoneNumber: $phoneNumber")
            return false
        }
    }
    return true
}

fun validatePhoneNumberRegex(phoneNumber: String): Boolean {
    val phoneNumberPattern = "^\\d$".toRegex()

    return phoneNumberPattern.matches(phoneNumber) && phoneNumber.length == 8
}

fun isFutureBibelGroupMeetingNextWeek(bibelGroupMeetingdate: LocalDate): Boolean {
    val currentDay = LocalDate.now()
    val daysBetween = abs(ChronoUnit.DAYS.between(bibelGroupMeetingdate, currentDay))
    return daysBetween < 7
}

fun nearestFutureBibelGroupMeeting(bibelgroupmeetings: List<BibleGroupMeeting>): BibleGroupMeeting? {
    val currentDay = LocalDate.now()

    return bibelgroupmeetings
        .filter { it.date.isAfter(currentDay) }
        .minByOrNull {
            abs(ChronoUnit.DAYS.between(it.date, currentDay))
        }

}


fun fetchBibleGroupMeetingFromGoogleSheets(googleSheetXlsxUrl: String): List<BibleGroupMeeting> {

    val bibleGroupMeetingsGoogleSheetUrl =
        URI.create(googleSheetXlsxUrl)
            .toURL().openConnection() as HttpURLConnection

    val bibleGroupMeetingsWorkbook: Workbook = XSSFWorkbook(bibleGroupMeetingsGoogleSheetUrl.inputStream)
    val bibleGroupMeetingsSheet1: Sheet = bibleGroupMeetingsWorkbook.getSheetAt(0)


    val bibleGroupMeetings = bibleGroupMeetingsSheet1
        .asSequence()
        .filter { !it.getCell(0).getStringValue().matches(Regex("Når")) }
        .filter { !it.getCell(0).getStringValue().matches(Regex("Hos hvem")) }
        .filter { !it.getCell(0).getStringValue().matches(Regex("Adresse")) }
        .filter { !it.getCell(0).getStringValue().matches(Regex("Tema")) }
        .filter { !it.getCell(0).getStringValue().matches(Regex("")) }
        .map { row ->
            BibleGroupMeeting(
                date = row.getCell(0).getStringValue().toLocalDate(),
                who = row.getCell(1).getStringValue(),
                address = row.getCell(2).getStringValue(),
                theme = row.getCell(3).getStringValue(),
            )
        }
        .toList()

    bibleGroupMeetingsGoogleSheetUrl.disconnect()

    return bibleGroupMeetings
}

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}

fun Cell.getStringValue(): String {

    return when (this.cellType) {
        CellType.STRING -> {
            if (this.stringCellValue.isNullOrBlank()) {
                ""
            } else {
                this.stringCellValue.toString()
            }
        }

        CellType.NUMERIC -> {
            this.numericCellValue.toInt().toString()
        }

        else -> {
            ""
        }
    }
}

fun createHtmlContentInStringFormat(bibelgroupmeeting: BibleGroupMeeting): String {

    return "<!DOCTYPE html>" +
            "<html lang=\"no\">" +
            "<head>" +
            "<title>Bibelgruppe den ${bibelgroupmeeting.date.format(dateFormatt)} påminnelse</title>" +
            "</head>" +
            "<body>Husk at det er bibelgruppe på onsdag!" +
            "<ul>" +
            "<li>Dato: ${bibelgroupmeeting.date.format(dateFormatt)}</li>" +
            "<li>Hos: ${bibelgroupmeeting.who}</li>" +
            "<li>Adresse: ${bibelgroupmeeting.address}</li>" +
            "<li>Kl: 19:30</li>" +
            "<li>Tema: ${bibelgroupmeeting.theme}</li>" +
            "</ul>" +
            "</body>" +
            "</html>"

}

fun smsNotify(accountSid: String, authToken: String, phoneNumbers: List<String>, bibelgroupmeeting: BibleGroupMeeting) {

    Twilio.init(accountSid, authToken)

    val message: Message? = Message
        .creator(
            PhoneNumber("+15558675309"),
            PhoneNumber("+15017250604"),
            "This is the ship that made the Kessel Run in fourteen parsecs?"
        )
        .create()


    println(message?.sid)

}
