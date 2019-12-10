# Automatically generated by pb2py
# fmt: off
from .. import protobuf as p

if __debug__:
    try:
        from typing import Dict, List, Optional
        from typing_extensions import Literal  # noqa: F401
        EnumTypeNEMImportanceTransferMode = Literal[1, 2]
    except ImportError:
        Dict, List, Optional = None, None, None  # type: ignore
        EnumTypeNEMImportanceTransferMode = None  # type: ignore


class NEMImportanceTransfer(p.MessageType):

    def __init__(
        self,
        mode: EnumTypeNEMImportanceTransferMode = None,
        public_key: bytes = None,
    ) -> None:
        self.mode = mode
        self.public_key = public_key

    @classmethod
    def get_fields(cls) -> Dict:
        return {
            1: ('mode', p.EnumType("NEMImportanceTransferMode", (1, 2)), 0),
            2: ('public_key', p.BytesType, 0),
        }
