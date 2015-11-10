/*
 * http://ryred.co/
 * ace[at]ac3-servers.eu
 *
 * =================================================================
 *
 * Copyright (c) 2015, Cory Redmond
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of SinusPls nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package co.ryred.sinuspls;

import lombok.Getter;
import org.apache.commons.cli.Option;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cory Redmond
 *         Created by acech_000 on 09/11/2015.
 */
@Getter
public enum OptEnum
{

	INPUT_FOLDER( "in", "inputFolder", true, "The folder full of mp3 files.\nWill use the current dir if not provided." ),
	OUTPUT_FOLDER( "out", "outFolder", true, "The folder to copy the files.\nThis is required." ),
	DB_FILE( "db", "dbFile", true, "The folder to copy the files.\nThis is required." ),
	HELP( "?", "help", false, "Shows this message." ),
	FIX( "f", "fix", false, "Only fix the broken files." );

	private final static Map<Option, OptEnum> BY_OPTION = new HashMap<>();

	static {
		for ( OptEnum option : OptEnum.values() ) BY_OPTION.put( option.getOption(), option );
	}

	private final String shortOpt;
	private final String longOpt;
	private final boolean hasArgs;
	private final String description;
	private final Option option;

	OptEnum( String shortOpt, String longOpt, boolean hasArgs, String description )
	{
		this.shortOpt = shortOpt;
		this.longOpt = longOpt;
		this.hasArgs = hasArgs;
		this.description = description;
		this.option = new Option( shortOpt, longOpt, hasArgs, description );
	}

	public static OptEnum getByOption( Option option )
	{
		return BY_OPTION.get( option );
	}

}
