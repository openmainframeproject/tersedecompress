.SECONDARY:
.SUFIXES:
.PHONY: all clean
default: all

include ../envdef.mak

OBJ_LIB_DIR=$(OBJ_DIR)/lib
LIBTERSEDECOMPRESS = $(BIN_DIR)/libtersedecompress.a
TERSEDECOMPRESS = $(BIN_DIR)/tersedecompress++
SRC_LIB=\
NonSpackDecompresser.cpp \
SpackDecompresser.cpp \
TerseBlockReader.cpp \
TerseDecompresser.cpp \
TerseHeader.cpp
SRC_APP=\
main.cpp \
argumentParser.cpp

OBJS_LIB += $(addprefix $(OBJ_LIB_DIR)/, $(addsuffix .o, $(basename $(SRC_LIB))))
OBJS += $(addprefix $(OBJ_DIR)/, $(addsuffix .o, $(basename $(SRC_APP))))
DEPS += $(addsuffix .d, $(basename $(OBJS)))

$(OBJ_DIR)/%.o: %.cpp | $(OBJ_DIR)
	$(CXX) $(CXXFLAGS) -DGIT_TAG=\"$(GIT_TAG)\" -MMD -MF $(OBJ_DIR)/$*.d -MP -c $< -o $@

$(OBJ_LIB_DIR)/%.o: %.cpp | $(OBJ_DIR) $(OBJ_LIB_DIR)
	$(CXX) $(CXXFLAGS) $(CCFLAGS_DLL) -MMD -MF $(OBJ_LIB_DIR)/$*.d -MP -c $< -o $@

$(LIBTERSEDECOMPRESS): $(OBJS_LIB) | $(BIN_DIR)
	$(AR) rcs $(LIBTERSEDECOMPRESS) $(OBJS_LIB)

$(TERSEDECOMPRESS): $(OBJS) $(LIBTERSEDECOMPRESS) | $(BIN_DIR)
	$(LD) $(LDFLAGS) -L$(BIN_DIR) -o $(TERSEDECOMPRESS) $(OBJS) $(LIBS)

$(OBJ_DIR) $(OBJ_LIB_DIR) $(BIN_DIR):
	-mkdir -p $@

all: $(TERSEDECOMPRESS)

clean:
	rm -rf $(OBJ_DIR) $(OBJS_LIB) $(BIN_DIR) $(OBJS) $(DEPS) $(LIBTERSEDECOMPRESS) $(TERSEDECOMPRESS)

-include $(OBJS:%.o=%.d)