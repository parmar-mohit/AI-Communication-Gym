package com.thinkschool.coach.constants;

public class FileLocation {
	public static final String SESSION_START_EVENT = "events/session_start.json";
	public static final String PROMPT_START_EVENT = "events/prompt_start.json";
	public static final String TEXT_CONTENT_START_EVENT = "events/text_content_start_event.json";
	public static final String AUDIO_CONTENT_START_EVENT = "events/audio_content_start_event.json";
	public static final String CONTENT_END_EVENT = "events/content_end_event.json";
	public static final String TEXT_INPUT_CONTENT = "events/text_input_content.json";
	public static final String AUDIO_INPUT_CONTENT = "events/audio_input_content.json";
	public static final String PROMPT_END_EVENT = "events/prompt_end_event.json";
	public static final String SESSION_END_EVENT = "events/session_end_event.json";
	public static final String SYSTEM_PROMPT = "prompts/system_prompt.txt";
	public static final String INTIAL_SESSION_AUDIO = "audio/session_start.wav";
	public static final String USER_PROMPT_SESSION_START = "prompts/user_prompt_session_start.txt";
	
	public static final String SESSION_VIDEO_PREFIX = System.getenv("SESSION_VIDEO_PREFIX");
	public static final String SESSION_TRANSCRIPT_PREFIX = System.getenv("SESSION_TRANSCRIPT_PREFIX");
	public static final String SESSION_IMAGE_PREFIX = System.getenv("SESSION_IMAGE_PREFIX");
}
