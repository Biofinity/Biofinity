package edu.unl.biofinity.site.snippet

import edu.unl.biofinity.api.{model => Model}

import java.util.Date

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.S._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import scala.xml._

class HDZ_BF {
	def initCreateOccurrence: NodeSeq = {
		val source: Model.Source = Model.Source.find(By(Model.Source.uniqueID, "HDZ_BUTTERFLY_PRIVATE")) openOr null
		if (null == source) {
			S.redirectTo("/HDZ/butterfly")
		} else {
			Model.Event.currentEvent(Model.Event.create.source(source))
			Model.Location.currentLocation(Model.Location.create.source(source))
			Model.Location.currentLocation.is.continent("NA")
			Model.Location.currentLocation.is.country("United States")
		}
		Text("")
	}
 	
	def initCreateOccurrenceMediaFile: NodeSeq = {
		var redirect = true
		val source: Model.Source = Model.Source.find(By(Model.Source.uniqueID, "HDZ_BUTTERFLY_PRIVATE")) openOr null
		if (null != source) {
			val occurrence = Model.Occurrence.find(S.param("OccurrenceID") openOr -1) openOr null
			if (null != occurrence && occurrence.source.obj.open_! == source) {
				redirect = false
				Model.Occurrence.currentOccurrence(occurrence)
			}
		}
		if (redirect) {
			S.redirectTo("/HDZ/butterfly")
		}
		Text("")
	}
        
        def renderClassifications(xhtml: NodeSeq) = {
                var foo = "";
                var source: Model.Source = Model.Source.find(By(Model.Source.uniqueID, "HDZ_BUTTERFLY_PRIVATE")) openOr null
                var classes = source.classifications
                bind(
                  "Classification", 
                  xhtml, 
                  "List" -> {(nodeSeq: NodeSeq) => {
                      val classNodeSeq = classes.flatMap(clazz => {
                          bind("Classification", nodeSeq, 
                               "Class" -> <span>{clazz.name}</span>, 
                               "Count" -> SHtml.ajaxText("", value => {foo = value; JsCmds.Noop})
                          )
                      })
                      classNodeSeq
                  }}
                )
        }
 	
	def renderCreateOccurrence(xhtml: NodeSeq): NodeSeq = {
	  	var airTemperature: String = ""
	  	var chytridSwabNumber: String = ""
	  	var commonName: String = ""
	  	var habitat: String = ""
	  	var school: String = ""
	  	var teacherName: String = ""
	  	var teacherEmail: String = ""
	  	var endTime: String = ""
                var sky: String = "";
                var wind: String = "";
    
                var skyList = List("Mostly Clear", "Mostly Cloudy", "Overcast", "Fog");
                var windList = List("Calm", "Slight Wind", "Moderate Wind", "Windy");
                var classMap = scala.collection.mutable.Map[edu.unl.biofinity.api.model.Classification, Int]();
                
		def save(): JsCmd = {
                        Alert("Processing please wait ...");
			if ("" == school) {
				Alert("Enter the school you attend.")
			} else if ("" == teacherName) {
				Alert("Enter your teacher's name.")
			} else if ("" == teacherEmail) {
				Alert("Enter your teacher's email.")
			} else if (null == Model.Location.currentLocation.is.stateProvince.is || "" == Model.Location.currentLocation.is.stateProvince.is) {
				Alert("Enter the state where the event occurred.")
			} else if (null == Model.Location.currentLocation.is.county.is || "" == Model.Location.currentLocation.is.county.is) {
				Alert("Enter the county where the event occurred.")
			} else if (null == Model.Location.currentLocation.is.latitude.is || 0.0 == Model.Location.currentLocation.is.latitude.is) {
				Alert("Latitude cannot be blank.  Drag the marker to the location where the event occurred.")
			} else if (null == Model.Location.currentLocation.is.longitude.is || 0.0 == Model.Location.currentLocation.is.longitude.is) {
				Alert("Longitude cannot be blank.  Drag the marker to the location where the event occurred.")
			} else if (null == Model.Event.currentEvent.is.date.is) {
				Alert("Enter a date.")
			} else if ("" == habitat) {
				Alert("Describe the habitat of the location.")
			} else {
				Model.Location.currentLocation.is.save
				
				Model.Event.currentEvent.is.location(Model.Location.currentLocation.is)
				Model.Event.currentEvent.is.habitat(habitat)
				var additionalPropertyBundle: Model.AdditionalPropertyBundle = Model.AdditionalPropertyBundle.create
				additionalPropertyBundle.save
				Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("Air Temperature").value(airTemperature).save
                                Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("End Time").value(endTime).save
				Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("Sky Conditions").value(sky).save
				Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("Wind Conditions").value(wind).save
                                Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("School Name").value(school).save
				Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("Teacher Name").value(teacherName).save
				Model.AdditionalProperty.create.additionalPropertyBundle(additionalPropertyBundle).name("Teacher Email").value(teacherEmail).save
                                
                                Model.Event.currentEvent.is.additionalPropertyBundle(additionalPropertyBundle)
				Model.Event.currentEvent.is.save
				
                                val source: Model.Source = Model.Source.find(By(Model.Source.uniqueID, "HDZ_BUTTERFLY_PRIVATE")) openOr null
                                classMap foreach { case(key, value) => {
                                      for ( i <- 0 until value ) {
                                        Model.Occurrence.currentOccurrence(Model.Occurrence.create.source(source).occurrenceType(Model.OccurrenceType.HumanObservation))
                                        Model.Occurrence.currentOccurrence.is.event(Model.Event.currentEvent.is)
                                        Model.Occurrence.currentOccurrence.is.classification(key)
                                        Model.Occurrence.currentOccurrence.is.save
                                      }
                                }}

				JsCmds.RedirectTo("/HDZ/butterfly/success?OccurrenceID=" + Model.Occurrence.currentOccurrence.is.entityID)
			}
		}
		
		SHtml.ajaxForm(
			bind(
				"butterfly",
				xhtml,
				"AirTemperature" -> SHtml.ajaxText(
					"",
					value => {
						val showAlert = (value != "" && value != airTemperature)
						airTemperature = value
						try {
							val temperature = value.toDouble
							JsCmds.Noop
						} catch {
							case e:NumberFormatException => {
								if (showAlert) {
									Alert("Air temperature must be a number.")
								} else {
									JsCmds.Noop
								}
							}
						}
					}
				),
				"Habitat" -> SHtml.ajaxText("", value => {habitat = value; JsCmds.Noop}),
				"School" -> SHtml.ajaxText("", value => {school = value; JsCmds.Noop}),
				"TeacherName" -> SHtml.ajaxText("", value => {teacherName = value; JsCmds.Noop}),
				"TeacherEmail" -> SHtml.ajaxText("", value => {teacherEmail = value; JsCmds.Noop}),
				"EndTime" -> SHtml.ajaxText("", value => {endTime = value; JsCmds.Noop}),                      
                                "Sky" -> SHtml.ajaxSelect(skyList.map(i=> (i,i)), Empty, value => { sky = value; JsCmds.Noop}), 
                                "Wind" -> SHtml.ajaxSelect(windList.map(i => (i,i)), Empty, (value:String) => { wind = value; JsCmds.Noop}),
				"Save" -> SHtml.ajaxButton("Save", () => save), 
                                "Classifications" -> {(nodeSeq: NodeSeq) => {
                                      var source: Model.Source = Model.Source.find(By(Model.Source.uniqueID, "HDZ_BUTTERFLY_PRIVATE")) openOr null
                                      var classes = source.classifications
                                      bind(
                                        "Classification", 
                                        nodeSeq, 
                                        "List" -> {(view: NodeSeq) => {
                                        val classNodeSeq = classes.flatMap(clazz => {
                                        bind("Classification", view, 
                                            "Class" -> <span>{clazz.name}</span>, 
                                            "Count" -> SHtml.ajaxText("0", value => {classMap(clazz) = Integer.parseInt(value); JsCmds.Noop})
                                        )
                                        })
                                        classNodeSeq
                                      }}
                                    )
				}}
			)
		)
	}
}