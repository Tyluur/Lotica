package com.runescape.cache.loaders;

import com.runescape.cache.Cache;
import com.runescape.network.stream.InputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ObjectDefinitions {

	private static final ConcurrentHashMap<Integer, ObjectDefinitions> objectDefinitions = new ConcurrentHashMap<Integer, ObjectDefinitions>();

	static int anInt3832;

	static int anInt3836;

	static int anInt3842;

	static int anInt3843;

	static int anInt3846;

	public boolean secondBool;

	public boolean aBoolean3853;

	public boolean notCliped;

	public String[] options;

	public int configFileId;

	public boolean projectileCliped;

	public int sizeY;

	public int thirdInt;

	public int anInt3876;

	public int clipType;

	public int sizeX;

	public boolean aBoolean3891;

	public int secondInt;

	public String name;

	public int[][] modelIds;

	public int id;

	int[] toObjectIds;

	int[] anIntArray3833 = null;

	int anInt3835;

	int anInt3838 = -1;

	boolean aBoolean3839;

	int anInt3844;

	boolean aBoolean3845;

	int anInt3850;

	int anInt3851;

	int anInt3855;

	int anInt3857;

	int[] anIntArray3859;

	int anInt3860;

	int anInt3865;

	boolean aBoolean3866;

	boolean aBoolean3867;

	boolean aBoolean3870;

	boolean aBoolean3872;

	boolean aBoolean3873;

	Object loader;

	int anInt3892;

	boolean aBoolean3894;

	boolean aBoolean3895;

	int anInt3896;

	int configId;

	int anInt3900;

	int anInt3904;

	int anInt3905;

	boolean aBoolean3906;

	int[] anIntArray3908;

	int anInt3913;

	int anInt3921;

	boolean aBoolean3923;

	boolean aBoolean3924;

	int cflag;

	private short[] originalColors;

	private int anInt3834;

	private byte aByte3837;

	private int anInt3840;

	private int anInt3841;

	private byte aByte3847;

	private byte aByte3849;

	private byte[] aByteArray3858;

	private short[] modifiedColors;

	private int[] anIntArray3869;

	private int anInt3875;

	private int anInt3877;

	private int anInt3878;

	private int anInt3882;

	private int anInt3883;

	private int anInt3889;

	private byte[] aByteArray3899;

	private int anInt3902;

	private byte aByte3914;

	private int anInt3915;

	private int anInt3917;

	private short[] aShortArray3919;

	private short[] aShortArray3920;

	private HashMap<Integer, Object> parameters;

	private ObjectDefinitions() {
		anInt3835 = -1;
		anInt3860 = -1;
		configFileId = -1;
		aBoolean3866 = false;
		anInt3851 = -1;
		anInt3865 = 255;
		aBoolean3845 = false;
		aBoolean3867 = false;
		anInt3850 = 0;
		anInt3844 = -1;
		anInt3857 = -1;
		aBoolean3872 = true;
		anInt3882 = -1;
		anInt3834 = 0;
		options = new String[5];
		anInt3875 = 0;
		aBoolean3839 = false;
		anIntArray3869 = null;
		sizeY = 1;
		thirdInt = -1;
		anInt3883 = 0;
		aBoolean3895 = true;
		anInt3840 = 0;
		aBoolean3870 = false;
		anInt3889 = 0;
		aBoolean3853 = true;
		secondBool = false;
		clipType = 2;
		projectileCliped = true;
		notCliped = false;
		anInt3855 = -1;
		anInt3878 = 0;
		anInt3904 = 0;
		sizeX = 1;
		anInt3876 = -1;
		aBoolean3891 = false;
		anInt3905 = 0;
		name = "null";
		anInt3913 = -1;
		aBoolean3906 = false;
		aBoolean3873 = false;
		aByte3914 = (byte) 0;
		anInt3915 = 0;
		anInt3900 = 0;
		secondInt = -1;
		aBoolean3894 = false;
		anInt3921 = 0;
		anInt3902 = 128;
		configId = -1;
		anInt3877 = 0;
		cflag = 0;
		anInt3892 = 64;
		aBoolean3923 = false;
		aBoolean3924 = false;
		anInt3841 = 128;
		anInt3917 = 128;
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (int i = 4903; i < 4904; i++) {
			ObjectDefinitions defs = getObjectDefinitions(i);
			// if (!defs.name.equals("null"))
			System.out.println(i + ", " + defs.configId);
		}
		/*
		 * System.out.println(defs.configId);
		 * System.out.println(defs.configFileId);
		 */

	}

	public static ObjectDefinitions getObjectDefinitions(int id) {
		ObjectDefinitions def = objectDefinitions.get(id);
		if (def == null) {
			def = new ObjectDefinitions();
			def.id = id;
			byte[] data = Cache.STORE.getIndexes()[16].getFile(getArchiveId(id), id & 0xff);
			if (data == null) {
				System.out.println("Failed loading Object " + id + ".");
			} else {
				def.readValueLoop(new InputStream(data));
			}
			def.method3287();
			if ((def.name != null && (def.name.equalsIgnoreCase("bank booth") || def.name.equalsIgnoreCase("counter")))) {
				def.notCliped = false;
				def.projectileCliped = true;
				if (def.clipType == 0) {
					def.clipType = 1;
				}
			}
			if (def.notCliped) {
				def.projectileCliped = false;
				def.clipType = 0;
			}
			objectDefinitions.put(id, def);
		}
		return def;
	}

	private static int getArchiveId(int i_0_) {
		return i_0_ >>> -1135990488;
	}

	private void readValueLoop(InputStream stream) {
		for (; ; ) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				// System.out.println("Remaining: "+stream.getRemaining());
				break;
			}
			readValues(stream, opcode);
		}
	}

	final void method3287() {
		if (secondInt == -1) {
			secondInt = 0;
			if (aByteArray3899 != null && aByteArray3899.length == 1 && aByteArray3899[0] == 10) {
				secondInt = 1;
			}
			for (int i_13_ = 0; i_13_ < 5; i_13_++) {
				if (options[i_13_] != null) {
					secondInt = 1;
					break;
				}
			}
		}
		if (anInt3855 == -1) {
			anInt3855 = clipType != 0 ? 1 : 0;
		}
	}

	private void readValues(InputStream stream, int opcode) {
		// System.out.println(opcode);
		if (opcode != 1 && opcode != 5) {
			if (opcode != 2) {
				if (opcode != 14) {
					if (opcode != 15) {
						if (opcode == 17) { // nocliped
							projectileCliped = false;
							clipType = 0;
						} else if (opcode != 18) {
							if (opcode == 19) {
								secondInt = stream.readUnsignedByte();
							} else if (opcode == 21) {
							} else if (opcode != 22) {
								if (opcode != 23) {
									if (opcode != 24) {
										if (opcode == 27) {
											// diff between 2
											// and 1
											clipType = 1;
										} else if (opcode == 28) {
											anInt3892 = (stream.readUnsignedByte() << 2);
										} else if (opcode != 29) {
											if (opcode != 39) {
												if (opcode < 30 || opcode >= 35) {
													if (opcode == 40) {
														int i_53_ = (stream.readUnsignedByte());
														originalColors = new short[i_53_];
														modifiedColors = new short[i_53_];
														for (int i_54_ = 0; i_53_ > i_54_; i_54_++) {
															originalColors[i_54_] = (short) (stream.readUnsignedShort());
															modifiedColors[i_54_] = (short) (stream.readUnsignedShort());
														}
													} else if (opcode != 41) { // object
														// anim
														if (opcode != 42) {
															if (opcode != 62) {
																if (opcode != 64) {
																	if (opcode == 65) {
																		anInt3902 = stream.readUnsignedShort();
																	} else if (opcode != 66) {
																		if (opcode != 67) {
																			if (opcode == 69) {
																				cflag = stream.readUnsignedByte();
																			} else if (opcode != 70) {
																				if (opcode == 71) {
																					anInt3889 = stream.readShort() << 2;
																				} else if (opcode != 72) {
																					if (opcode == 73) {
																						secondBool = true;
																					} else if (opcode == 74) {
																						notCliped = true;
																					} else if (opcode != 75) {
																						if (opcode != 77 && opcode != 92) {
																							if (opcode == 78) {
																								anInt3860 = stream.readUnsignedShort();
																								anInt3904 = stream.readUnsignedByte();
																							} else if (opcode != 79) {
																								if (opcode == 81) {
																									anInt3882 = 256 * stream.readUnsignedByte();
																								} else if (opcode != 82) {
																									if (opcode == 88) {
																										aBoolean3853 = false;
																									} else if (opcode != 89) {
																										if (opcode == 90) {
																											aBoolean3870 = true;
																										} else if (opcode != 91) {
																											if (opcode != 93) {
																												if (opcode == 94) {
																												} else if (opcode != 95) {
																													if (opcode != 96) {
																														if (opcode == 97) {
																															aBoolean3866 = true;
																														} else if (opcode == 98) {
																															aBoolean3923 = true;
																														} else if (opcode == 99) {
																															anInt3857 = stream.readUnsignedByte();
																															anInt3835 = stream.readUnsignedShort();
																														} else if (opcode == 100) {
																															anInt3844 = stream.readUnsignedByte();
																															anInt3913 = stream.readUnsignedShort();
																														} else if (opcode != 101) {
																															if (opcode == 102) {
																																anInt3838 = stream.readUnsignedShort();
																															} else if (opcode == 103) {
																																thirdInt = 0;
																															} else if (opcode != 104) {
																																if (opcode == 105) {
																																	aBoolean3906 = true;
																																} else if (opcode == 106) {
																																	int i_55_ = stream.readUnsignedByte();
																																	anIntArray3869 = new int[i_55_];
																																	anIntArray3833 = new int[i_55_];
																																	for (int i_56_ = 0; i_56_ < i_55_; i_56_++) {
																																		anIntArray3833[i_56_] = stream.readUnsignedShort();
																																		int i_57_ = stream.readUnsignedByte();
																																		anIntArray3869[i_56_] = i_57_;
																																	}
																																} else if (opcode == 107) {
																																	anInt3851 = stream.readUnsignedShort();
																																} else if (opcode >= 150 && opcode < 155) {
																																	options[opcode + -150] = stream.readString();
																																	/*
																																	 * if
																																	 * (
																																	 * !
																																	 * loader
																																	 * .
																																	 * showOptions
																																	 * )
																																	 * options
																																	 * [
																																	 * opcode
																																	 * +
																																	 * -
																																	 * 150
																																	 * ]
																																	 * =
																																	 * null
																																	 * ;
																																	 */
																																} else if (opcode != 160) {
																																	if (opcode == 162) {
																																		anInt3882 = stream.readInt();
																																	} else if (opcode == 163) {
																																		aByte3847 = (byte) stream.readByte();
																																		aByte3849 = (byte) stream.readByte();
																																		aByte3837 = (byte) stream.readByte();
																																		aByte3914 = (byte) stream.readByte();
																																	} else if (opcode != 164) {
																																		if (opcode != 165) {
																																			if (opcode != 166) {
																																				if (opcode == 167) {
																																					anInt3921 = stream.readUnsignedShort();
																																				} else if (opcode != 168) {
																																					if (opcode == 169) {
																																						aBoolean3845 = true;
																																						// added
																																						// opcode
																																					} else if (opcode == 170) {
																																						stream.readUnsignedSmart();
																																					} else if (opcode == 171) {
																																						stream.readUnsignedSmart();
																																					} else if (opcode == 173) {
																																						stream.readUnsignedShort();
																																						stream.readUnsignedShort();
																																					} else if (opcode == 177) {
																																					} else if (opcode == 178) {
																																						stream.readUnsignedByte();
																																					} else if (opcode == 189) {
																																					} else if (opcode == 249) {
																																						int length = stream.readUnsignedByte();
																																						if (parameters == null) {
																																							parameters = new HashMap<Integer, Object>(length);
																																						}
																																						for (int i_60_ = 0; i_60_ < length; i_60_++) {
																																							boolean bool = stream.readUnsignedByte() == 1;
																																							int i_61_ = stream.read24BitInt();
																																							if (!bool) {
																																								parameters.put(i_61_, stream.readInt());
																																							} else {
																																								parameters.put(i_61_, stream.readString());
																																							}

																																						}
																																					}
																																				} else {
																																					aBoolean3894 = true;
																																				}
																																			} else {
																																				anInt3877 = stream.readShort();
																																			}
																																		} else {
																																			anInt3875 = stream.readShort();
																																		}
																																	} else {
																																		anInt3834 = stream.readShort();
																																	}
																																} else {
																																	int i_62_ = stream.readUnsignedByte();
																																	anIntArray3908 = new int[i_62_];
																																	for (int i_63_ = 0; i_62_ > i_63_; i_63_++) {
																																		anIntArray3908[i_63_] = stream.readUnsignedShort();
																																	}
																																}
																															} else {
																																anInt3865 = stream.readUnsignedByte();
																															}
																														} else {
																															anInt3850 = stream.readUnsignedByte();
																														}
																													} else {
																														aBoolean3924 = true;
																													}
																												} else {
																													anInt3882 = stream.readShort();
																												}
																											} else {
																												anInt3882 = stream.readUnsignedShort();
																											}
																										} else {
																											aBoolean3873 = true;
																										}
																									} else {
																										aBoolean3895 = false;
																									}
																								} else {
																									aBoolean3891 = true;
																								}
																							} else {
																								anInt3900 = stream.readUnsignedShort();
																								anInt3905 = stream.readUnsignedShort();
																								anInt3904 = stream.readUnsignedByte();
																								int i_64_ = stream.readUnsignedByte();
																								anIntArray3859 = new int[i_64_];
																								for (int i_65_ = 0; i_65_ < i_64_; i_65_++) {
																									anIntArray3859[i_65_] = stream.readUnsignedShort();
																								}
																							}
																						} else {
																							configFileId = stream.readUnsignedShort();
																							if (configFileId == 65535) {
																								configFileId = -1;
																							}
																							configId = stream.readUnsignedShort();
																							if (configId == 65535) {
																								configId = -1;
																							}
																							int i_66_ = -1;
																							if (opcode == 92) {
																								i_66_ = stream.readUnsignedShort();
																							}
																							int i_67_ = stream.readUnsignedByte();
																							toObjectIds = new int[i_67_ - -2];
																							for (int i_68_ = 0; i_67_ >= i_68_; i_68_++) {
																								toObjectIds[i_68_] = stream.readUnsignedShort();
																							}
																							toObjectIds[i_67_ + 1] = i_66_;
																						}
																					} else {
																						anInt3855 = stream.readUnsignedByte();
																					}
																				} else {
																					anInt3915 = stream.readShort() << 2;
																				}
																			} else {
																				anInt3883 = stream.readShort() << 2;
																			}
																		} else {
																			anInt3917 = stream.readUnsignedShort();
																		}
																	} else {
																		anInt3841 = stream.readUnsignedShort();
																	}
																} else {
																	// 64
																	aBoolean3872 = false;
																}
															} else {
																aBoolean3839 = true;
															}
														} else {
															int i_69_ = (stream.readUnsignedByte());
															aByteArray3858 = (new byte[i_69_]);
															for (int i_70_ = 0; i_70_ < i_69_; i_70_++) {
																aByteArray3858[i_70_] = (byte) (stream.readByte());
															}
														}
													} else {
														int i_71_ = (stream.readUnsignedByte());
														aShortArray3920 = new short[i_71_];
														aShortArray3919 = new short[i_71_];
														for (int i_72_ = 0; i_71_ > i_72_; i_72_++) {
															aShortArray3920[i_72_] = (short) (stream.readUnsignedShort());
															aShortArray3919[i_72_] = (short) (stream.readUnsignedShort());
														}
													}
												} else {
													options[-30 + opcode] = (stream.readString());
												}
											} else {
												// 39
												anInt3840 = (stream.readByte() * 5);
											}
										} else {// 29
											anInt3878 = stream.readByte();
										}
									} else {
										anInt3876 = stream.readUnsignedShort();
									}
								} else {
									thirdInt = 1;
								}
							} else {
								aBoolean3867 = true;
							}
						} else {
							projectileCliped = false;
						}
					} else {
						// 15
						sizeY = stream.readUnsignedByte();
					}
				} else {
					// 14
					sizeX = stream.readUnsignedByte();
				}
			} else {
				name = stream.readString();
			}
		} else {
			boolean aBoolean1162 = false;
			if (opcode == 5 && aBoolean1162) {
				skipReadModelIds(stream);
			}
			int i_73_ = stream.readUnsignedByte();
			modelIds = new int[i_73_][];
			aByteArray3899 = new byte[i_73_];
			for (int i_74_ = 0; i_74_ < i_73_; i_74_++) {
				aByteArray3899[i_74_] = (byte) stream.readByte();
				int i_75_ = stream.readUnsignedByte();
				modelIds[i_74_] = new int[i_75_];
				for (int i_76_ = 0; i_75_ > i_76_; i_76_++) {
					modelIds[i_74_][i_76_] = stream.readUnsignedShort();
				}
			}
			if (opcode == 5 && !aBoolean1162) {
				skipReadModelIds(stream);
			}
		}
	}

	private void skipReadModelIds(InputStream stream) {
		int length = stream.readUnsignedByte();
		for (int index = 0; index < length; index++) {
			stream.skip(1);
			int length2 = stream.readUnsignedByte();
			for (int i = 0; i < length2; i++) {
				stream.readUnsignedShort();
			}
		}
	}

	public static void clearObjectDefinitions() {
		objectDefinitions.clear();
	}

	public String getFirstOption() {
		if (options == null || options.length < 1) {
			return "";
		}
		return options[0];
	}

	public String getSecondOption() {
		if (options == null || options.length < 2) {
			return "";
		}
		return options[1];
	}

	public String getOption(int option) {
		if (options == null || options.length < option || option == 0) {
			return "";
		}
		return options[option - 1];
	}

	public String getThirdOption() {
		if (options == null || options.length < 3) {
			return "";
		}
		return options[2];
	}

	public boolean containsOption(int i, String option) {
		if (options == null || options.length <= i || options[i] == null) {
			return false;
		}
		return options[i].equals(option);
	}

	public boolean containsOption(String o) {
		if (options == null) {
			return false;
		}
		for (String option : options) {
			if (option == null) {
				continue;
			}
			if (option.equalsIgnoreCase(o)) {
				return true;
			}
		}
		return false;
	}

	public int getClipType() {
		return clipType;
	}

	public boolean isProjectileCliped() {
		return projectileCliped;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public int getAccessBlockFlag() {
		return cflag;
	}

    public String[] getOptions() {
        return this.options;
    }

    public int getConfigFileId() {
        return this.configFileId;
    }
}
